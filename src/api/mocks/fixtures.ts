import type { OptionCode, Product, Quote, QuoteRequest, QuoteLayer, ShippingOption } from '../../types/api'

export const MOCK_LATENCY_MS = 350

export const MOCK_USERNAME = 'demo'
export const MOCK_PASSWORD = 'demo123'
export const MOCK_TOKEN = 'mock-barniz-express-token'

/** The Decorator wrapping order, innermost first. */
const LAYER_ORDER: OptionCode[] = ['FRAGILE', 'INSURANCE', 'CUSTOMS', 'GIFT', 'EXPRESS']

export const PRODUCTS: Product[] = [
  {
    id: 'olla-mopa',
    name: 'Mopa-Mopa Lidded Olla',
    description: 'Hand-lacquered lidded jar with gold filigree scrollwork and green mopa-mopa inlay.',
    basePriceCop: 180000,
    weightKg: 1.2,
    imageUrl: '/products/olla.svg',
  },
  {
    id: 'bandeja-filigrana',
    name: 'Filigree Serving Tray',
    description: 'Oval serving tray, deep black lacquer with interlaced gold vines on the rim.',
    basePriceCop: 240000,
    weightKg: 1.6,
    imageUrl: '/products/bandeja.svg',
  },
  {
    id: 'espejo-pasto',
    name: 'Pasto Mirror Frame',
    description: 'Wall mirror framed in barniz de Pasto with geometric green and gold mosaic bands.',
    basePriceCop: 320000,
    weightKg: 2.4,
    imageUrl: '/products/espejo.svg',
  },
  {
    id: 'portavelas-noche',
    name: 'Night Candle Holder',
    description: 'Small candle holder with red lacquer accents and hand-painted gold dots.',
    basePriceCop: 120000,
    weightKg: 0.8,
    imageUrl: '/products/portavelas.svg',
  },
]

export const OPTIONS: ShippingOption[] = [
  {
    code: 'FRAGILE',
    name: 'Fragile packaging',
    description: 'Custom-cut foam shell with reinforced corners, wrapped around the piece.',
    pricingRule: '+5% of base cost',
    incompatibleWith: ['EXPRESS'],
  },
  {
    code: 'INSURANCE',
    name: 'Insurance',
    description: 'Covers the declared value if the piece is lost or damaged in transit.',
    pricingRule: '+1.5% of declared value',
    incompatibleWith: [],
  },
  {
    code: 'CUSTOMS',
    name: 'Export customs',
    description: 'Paperwork, HS classification and clearance for international shipments.',
    pricingRule: 'Flat 38,000 COP',
    incompatibleWith: [],
  },
  {
    code: 'GIFT',
    name: 'Gift wrap',
    description: 'Ribbon, wax seal and a printed card carrying your gift message.',
    pricingRule: 'Flat 15,000 COP',
    incompatibleWith: [],
  },
  {
    code: 'EXPRESS',
    name: 'Express delivery',
    description: 'Priority handling and the fastest courier lane available.',
    pricingRule: '+25% of base cost',
    incompatibleWith: ['FRAGILE'],
  },
]

export class MockApiError extends Error {
  readonly status: number
  readonly code: string

  constructor(message: string, code = 'bad_request', status = 400) {
    super(message)
    this.name = 'MockApiError'
    this.code = code
    this.status = status
  }
}

function roundCop(value: number): number {
  return Math.round(value)
}

function baseCostCop(product: Product): number {
  return roundCop(product.basePriceCop * 0.06 + product.weightKg * 4500)
}

const money = new Intl.NumberFormat('en-US')

export function buildMockQuote(payload: QuoteRequest): Quote {
  const product = PRODUCTS.find((candidate) => candidate.id === payload.productId)
  if (!product) {
    throw new MockApiError('The selected product does not exist.', 'unknown_product')
  }

  const city = payload.destination?.city?.trim() ?? ''
  const country = payload.destination?.country?.trim() ?? ''
  if (!city || !country) {
    throw new MockApiError('Destination city and country are both required.', 'missing_destination')
  }

  if (!Number.isFinite(payload.declaredValueCop) || payload.declaredValueCop < 1000) {
    throw new MockApiError('Declared value must be at least 1,000 COP.', 'invalid_declared_value')
  }

  const selected = payload.options ?? []
  const unknown = selected.find(
    (code) => !OPTIONS.some((option) => option.code === code),
  )
  if (unknown) {
    throw new MockApiError(`Unknown shipping option "${unknown}".`, 'unknown_option')
  }

  if (selected.includes('EXPRESS') && selected.includes('FRAGILE')) {
    throw new MockApiError(
      'Fragile packaging cannot be combined with express delivery.',
      'incompatible_options',
    )
  }

  const giftMessage = payload.giftMessage?.trim() ?? ''
  if (selected.includes('GIFT') && giftMessage.length > 200) {
    throw new MockApiError('Gift message must be 200 characters or fewer.', 'gift_message_too_long')
  }

  const base = baseCostCop(product)
  const layers: QuoteLayer[] = []

  for (const code of LAYER_ORDER) {
    if (!selected.includes(code)) continue

    switch (code) {
      case 'FRAGILE':
        layers.push({
          code,
          label: 'Fragile foam shell',
          costCop: roundCop(base * 0.05),
          notes: ['Custom-cut foam fitted to the piece', 'Reinforced corners'],
        })
        break
      case 'INSURANCE':
        layers.push({
          code,
          label: 'Insurance seal',
          costCop: roundCop(payload.declaredValueCop * 0.015),
          notes: [`Coverage up to ${money.format(payload.declaredValueCop)} COP`],
        })
        break
      case 'CUSTOMS':
        layers.push({
          code,
          label: 'Export customs tag',
          costCop: 38000,
          notes: [`Declared for clearance in ${country}`, 'HS code classification included'],
        })
        break
      case 'GIFT':
        layers.push({
          code,
          label: 'Gift wrap',
          costCop: 15000,
          notes: giftMessage
            ? ['Ribbon and wax seal', 'Gift message printed on the card']
            : ['Ribbon and wax seal', 'Card included without a message'],
        })
        break
      case 'EXPRESS':
        layers.push({
          code,
          label: 'Express band',
          costCop: roundCop(base * 0.25),
          notes: ['Priority handling', 'Estimated 2-3 business days'],
        })
        break
    }
  }

  const totalCop = layers.reduce((sum, layer) => sum + layer.costCop, base)

  return {
    currency: 'COP',
    baseCostCop: base,
    totalCop,
    layers,
  }
}
