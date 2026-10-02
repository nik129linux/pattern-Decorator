import type { ApiErrorBody, AuthResponse } from '../types/api'

export const API_BASE_URL: string =
  (import.meta.env.VITE_API_URL as string | undefined) ?? 'http://localhost:8080'

const LOGIN_PATH = '/api/v1/auth/login'
const DEMO_CREDENTIALS = { username: 'demo', password: 'demo123' }

/** Token lives in memory only; a 401 triggers a fresh sign-in. */
let token: string | null = null
let pendingLogin: Promise<string> | null = null

export class ApiError extends Error {
  readonly status: number
  readonly code: string

  constructor(message: string, status: number, code = 'unknown_error') {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

async function readBody(response: Response): Promise<unknown> {
  const text = await response.text()
  if (!text) return null
  try {
    return JSON.parse(text)
  } catch {
    return null
  }
}

function fallbackMessage(status: number): string {
  if (status === 0) return 'Could not reach the server. Please try again.'
  if (status === 401) return 'Sign-in failed. Check that the backend is running.'
  if (status === 404) return 'The requested resource was not found.'
  if (status >= 500) return 'The server had a problem. Please try again shortly.'
  return 'The request could not be completed.'
}

/** POST /api/v1/auth/login with the demo credentials. Concurrent calls share one request. */
async function signIn(): Promise<string> {
  if (pendingLogin) return pendingLogin

  pendingLogin = (async () => {
    let response: Response
    try {
      response = await fetch(`${API_BASE_URL}${LOGIN_PATH}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        body: JSON.stringify(DEMO_CREDENTIALS),
      })
    } catch {
      throw new ApiError(fallbackMessage(0), 0, 'network_error')
    }

    const data = (await readBody(response)) as AuthResponse | ApiErrorBody | null
    if (!response.ok) {
      const message = data && 'message' in data ? data.message : fallbackMessage(response.status)
      throw new ApiError(message, response.status, data && 'error' in data ? data.error : 'login_failed')
    }

    const auth = data as AuthResponse
    token = auth.token
    return token
  })()

  try {
    return await pendingLogin
  } finally {
    pendingLogin = null
  }
}

async function ensureToken(): Promise<string> {
  if (token) return token
  return signIn()
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
}

async function send(path: string, method: RequestOptions['method'], body: unknown): Promise<Response> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`

  try {
    return await fetch(`${API_BASE_URL}${path}`, {
      method: method ?? 'GET',
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError(fallbackMessage(0), 0, 'network_error')
  }
}

/**
 * Plain fetch wrapper: auto-signs in with the demo user on the first call,
 * attaches the bearer token to every route and re-signs in once on 401.
 */
export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body } = options

  await ensureToken()

  let response = await send(path, method, body)

  if (response.status === 401) {
    token = null
    await ensureToken()
    response = await send(path, method, body)
  }

  const data = await readBody(response)

  if (!response.ok) {
    const payload = data as Partial<ApiErrorBody> | null
    const message = payload?.message?.trim() || fallbackMessage(response.status)
    const code = payload?.error ?? `http_${response.status}`
    throw new ApiError(message, response.status, code)
  }

  return data as T
}
