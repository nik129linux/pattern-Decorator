import type { Quote, QuoteRequest } from '../types/api'
import { request } from './client'

export function createQuote(payload: QuoteRequest): Promise<Quote> {
  return request<Quote>('/api/v1/quotes', {
    method: 'POST',
    body: payload,
  })
}
