/**
 * Types mirroring the Barniz Express REST contract exactly.
 * Keep these in sync with the backend teammate's OpenAPI/docs.
 */

export interface Product {
  id: string
  name: string
  description: string
  basePriceCop: number
  weightKg: number
  imageUrl: string
}

export type OptionCode = 'FRAGILE' | 'INSURANCE' | 'CUSTOMS' | 'GIFT' | 'EXPRESS'

export interface ShippingOption {
  code: OptionCode
  name: string
  description: string
  pricingRule: string
  incompatibleWith: string[]
}

export interface QuoteDestination {
  city: string
  country: string
}

export interface QuoteRequest {
  productId: string
  destination: QuoteDestination
  declaredValueCop: number
  options: string[]
  giftMessage?: string
}

export interface QuoteLayer {
  code: string
  label: string
  costCop: number
  notes: string[]
}

export interface Quote {
  currency: 'COP'
  baseCostCop: number
  totalCop: number
  /** Ordered from innermost to outermost layer. */
  layers: QuoteLayer[]
}

export interface AuthResponse {
  token: string
  expiresAt: string
}

export interface ApiErrorBody {
  error: string
  message: string
}
