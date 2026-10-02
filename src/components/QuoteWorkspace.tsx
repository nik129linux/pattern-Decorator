import { useEffect, useMemo, useRef, useState } from 'react'
import { getOptions, getProducts } from '../api/catalog'
import { createQuote } from '../api/quotes'
import NestedBoxes from './NestedBoxes'
import QuotePanel, { type FormState } from './QuotePanel'
import type { Product, Quote, QuoteRequest, ShippingOption } from '../types/api'

const QUOTE_DEBOUNCE_MS = 300

const INITIAL_FORM: FormState = {
  productId: '',
  city: 'Pasto',
  country: 'Colombia',
  declaredValue: '250000',
  options: [],
  giftMessage: '',
}

/** Maps the form to a quote request, or null while the form is incomplete. */
function buildQuoteRequest(form: FormState): QuoteRequest | null {
  const productId = form.productId.trim()
  const city = form.city.trim()
  const country = form.country.trim()
  const declaredValueCop = Math.round(Number(form.declaredValue))

  if (!productId || !city || !country) return null
  if (!Number.isFinite(declaredValueCop) || declaredValueCop < 1) return null

  const request: QuoteRequest = {
    productId,
    destination: { city, country },
    declaredValueCop,
    options: [...form.options],
  }

  if (form.options.includes('GIFT')) {
    const message = form.giftMessage.trim()
    if (message) request.giftMessage = message
  }

  return request
}

function errorMessage(error: unknown, fallback: string): string {
  return error instanceof Error && error.message ? error.message : fallback
}

export default function QuoteWorkspace() {
  const [products, setProducts] = useState<Product[]>([])
  const [shippingOptions, setShippingOptions] = useState<ShippingOption[]>([])
  const [catalogLoading, setCatalogLoading] = useState(true)
  const [catalogError, setCatalogError] = useState<string | null>(null)

  const [form, setForm] = useState<FormState>(INITIAL_FORM)
  const [quote, setQuote] = useState<Quote | null>(null)
  const [quoteLoading, setQuoteLoading] = useState(false)
  const [quoteError, setQuoteError] = useState<string | null>(null)

  const requestId = useRef(0)

  useEffect(() => {
    let cancelled = false

    Promise.all([getProducts(), getOptions()])
      .then(([productList, optionList]) => {
        if (cancelled) return
        setProducts(productList)
        setShippingOptions(optionList)
        setCatalogLoading(false)
        setForm((current) =>
          current.productId ? current : { ...current, productId: productList[0]?.id ?? '' },
        )
      })
      .catch((error: unknown) => {
        if (cancelled) return
        setCatalogLoading(false)
        setCatalogError(errorMessage(error, 'Could not load the product catalog.'))
      })

    return () => {
      cancelled = true
    }
  }, [])

  const request = useMemo(() => buildQuoteRequest(form), [
    form.productId,
    form.city,
    form.country,
    form.declaredValue,
    form.options,
    form.giftMessage,
  ])
  const serialized = request === null ? null : JSON.stringify(request)

  useEffect(() => {
    if (serialized === null) {
      setQuoteLoading(false)
      return
    }

    const id = ++requestId.current
    setQuoteLoading(true)
    setQuoteError(null)

    const timer = setTimeout(() => {
      createQuote(JSON.parse(serialized) as QuoteRequest)
        .then((data) => {
          if (id !== requestId.current) return
          setQuote(data)
          setQuoteLoading(false)
        })
        .catch((error: unknown) => {
          if (id !== requestId.current) return
          setQuoteLoading(false)
          setQuoteError(errorMessage(error, 'The quote could not be calculated.'))
        })
    }, QUOTE_DEBOUNCE_MS)

    return () => clearTimeout(timer)
  }, [serialized])

  const patchForm = (patch: Partial<FormState>) => setForm((current) => ({ ...current, ...patch }))

  const toggleOption = (code: string) =>
    setForm((current) => ({
      ...current,
      options: current.options.includes(code)
        ? current.options.filter((item) => item !== code)
        : [...current.options, code],
    }))

  const selectedProduct = products.find((product) => product.id === form.productId)

  return (
    <main className="grid flex-1 items-start gap-6 py-6 lg:grid-cols-2">
      <NestedBoxes
        product={selectedProduct}
        layers={quote?.layers ?? []}
        loading={quoteLoading || catalogLoading}
        error={quoteError && !quote ? quoteError : null}
      />
      <QuotePanel
        form={form}
        onChange={patchForm}
        onToggleOption={toggleOption}
        products={products}
        options={shippingOptions}
        catalogError={catalogError}
        quote={quote}
        quoteLoading={quoteLoading}
        quoteError={quoteError}
      />
    </main>
  )
}
