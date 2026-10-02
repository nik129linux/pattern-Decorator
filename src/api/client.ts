import type { ApiErrorBody } from '../types/api'
import { clearSession, getToken, notifyUnauthorized } from './session'

export const API_BASE_URL: string =
  (import.meta.env.VITE_API_URL as string | undefined) ?? 'http://localhost:8080'

/** Mocks stay on unless explicitly disabled with VITE_USE_MOCKS=false. */
export const USE_MOCKS: boolean = String(import.meta.env.VITE_USE_MOCKS ?? 'true').toLowerCase() !== 'false'

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

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  /** Every route except login requires the bearer token. */
  auth?: boolean
}

function fallbackMessage(status: number): string {
  if (status === 0) return 'Could not reach the server. Please try again.'
  if (status === 401) return 'Your session has expired. Please sign in again.'
  if (status === 403) return 'You are not allowed to perform this action.'
  if (status === 404) return 'The requested resource was not found.'
  if (status >= 500) return 'The server had a problem. Please try again shortly.'
  return 'The request could not be completed.'
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, auth = true } = options

  const headers: Record<string, string> = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (auth) {
    const token = getToken()
    if (token) headers.Authorization = `Bearer ${token}`
  }

  let response: Response
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError(fallbackMessage(0), 0, 'network_error')
  }

  if (response.status === 401 && auth) {
    clearSession()
    notifyUnauthorized()
  }

  const text = await response.text()
  let data: unknown = null
  if (text) {
    try {
      data = JSON.parse(text)
    } catch {
      data = null
    }
  }

  if (!response.ok) {
    const payload = data as Partial<ApiErrorBody> | null
    const message = payload?.message?.trim() || fallbackMessage(response.status)
    const code = payload?.error ?? `http_${response.status}`
    throw new ApiError(message, response.status, code)
  }

  return data as T
}
