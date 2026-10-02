import { delay, http, HttpResponse } from 'msw'
import type { QuoteRequest } from '../../types/api'
import {
  buildMockQuote,
  MockApiError,
  MOCK_LATENCY_MS,
  MOCK_PASSWORD,
  MOCK_TOKEN,
  MOCK_USERNAME,
  OPTIONS,
  PRODUCTS,
} from './fixtures'

function unauthorized() {
  return HttpResponse.json(
    { error: 'unauthorized', message: 'Your session has expired. Please sign in again.' },
    { status: 401 },
  )
}

function requireAuth(request: Request): Response | null {
  const header = request.headers.get('Authorization')
  if (header === `Bearer ${MOCK_TOKEN}`) return null
  return unauthorized()
}

export const handlers = [
  http.post('*/api/v1/auth/login', async ({ request }) => {
    await delay(MOCK_LATENCY_MS)
    const body = (await request.json()) as { username?: string; password?: string }
    if (body.username !== MOCK_USERNAME || body.password !== MOCK_PASSWORD) {
      return HttpResponse.json(
        { error: 'invalid_credentials', message: 'Invalid username or password.' },
        { status: 401 },
      )
    }
    return HttpResponse.json({
      token: MOCK_TOKEN,
      expiresAt: new Date(Date.now() + 8 * 60 * 60 * 1000).toISOString(),
    })
  }),

  http.get('*/api/v1/products', async ({ request }) => {
    await delay(MOCK_LATENCY_MS)
    const denied = requireAuth(request)
    if (denied) return denied
    return HttpResponse.json(PRODUCTS)
  }),

  http.get('*/api/v1/options', async ({ request }) => {
    await delay(MOCK_LATENCY_MS)
    const denied = requireAuth(request)
    if (denied) return denied
    return HttpResponse.json(OPTIONS)
  }),

  http.post('*/api/v1/quotes', async ({ request }) => {
    await delay(MOCK_LATENCY_MS)
    const denied = requireAuth(request)
    if (denied) return denied

    let payload: QuoteRequest
    try {
      payload = (await request.json()) as QuoteRequest
    } catch {
      return HttpResponse.json(
        { error: 'invalid_json', message: 'The request body is not valid JSON.' },
        { status: 400 },
      )
    }

    try {
      return HttpResponse.json(buildMockQuote(payload))
    } catch (error) {
      if (error instanceof MockApiError) {
        return HttpResponse.json({ error: error.code, message: error.message }, { status: error.status })
      }
      throw error
    }
  }),
]
