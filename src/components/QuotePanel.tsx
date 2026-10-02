import type { ReactNode } from 'react'
import OptionToggles from './OptionToggles'
import ProductPicker from './ProductPicker'
import { formatCop, inputClass, sectionTitleClass } from './ui'
import type { Product, Quote, ShippingOption } from '../types/api'

export interface FormState {
  productId: string
  city: string
  country: string
  declaredValue: string
  options: string[]
  giftMessage: string
}

interface QuotePanelProps {
  form: FormState
  onChange: (patch: Partial<FormState>) => void
  onToggleOption: (code: string) => void
  products: Product[]
  options: ShippingOption[]
  catalogError: string | null
  quote: Quote | null
  quoteLoading: boolean
  quoteError: string | null
}

function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="border-b border-line py-5 first:pt-0 last:border-b-0">
      <h2 className={sectionTitleClass}>{title}</h2>
      {children}
    </section>
  )
}

export default function QuotePanel({
  form,
  onChange,
  onToggleOption,
  products,
  options,
  catalogError,
  quote,
  quoteLoading,
  quoteError,
}: QuotePanelProps) {
  const giftEnabled = form.options.includes('GIFT')

  return (
    <div className="rounded-2xl border border-line bg-ink-soft/50 p-5 sm:p-6">
      {catalogError ? (
        <p role="alert" className="mb-4 rounded-md border border-crimson/60 bg-crimson/15 px-3 py-2 text-sm">
          {catalogError}
        </p>
      ) : null}

      <Section title="1. Product">
        <ProductPicker products={products} selectedId={form.productId} onChange={(productId) => onChange({ productId })} />
      </Section>

      <Section title="2. Destination">
        <div className="grid gap-3 sm:grid-cols-2">
          <label className="block text-sm">
            <span className="mb-1 block text-paper/70">City</span>
            <input
              className={inputClass}
              value={form.city}
              onChange={(event) => onChange({ city: event.target.value })}
              placeholder="Pasto"
              autoComplete="address-level2"
            />
          </label>
          <label className="block text-sm">
            <span className="mb-1 block text-paper/70">Country</span>
            <input
              className={inputClass}
              value={form.country}
              onChange={(event) => onChange({ country: event.target.value })}
              placeholder="Colombia"
              autoComplete="country-name"
            />
          </label>
        </div>
      </Section>

      <Section title="3. Declared value">
        <label className="block text-sm">
          <span className="mb-1 block text-paper/70">Value in COP</span>
          <input
            className={inputClass}
            type="number"
            min={1000}
            step={1000}
            inputMode="numeric"
            value={form.declaredValue}
            onChange={(event) => onChange({ declaredValue: event.target.value })}
            placeholder="250000"
          />
        </label>
      </Section>

      <Section title="4. Shipping options">
        <OptionToggles options={options} selected={form.options} onToggle={onToggleOption} />
      </Section>

      <Section title="5. Gift message">
        <label className="block text-sm">
          <span className="mb-1 block text-paper/70">Message printed on the card</span>
          <textarea
            className={`${inputClass} min-h-24 resize-y`}
            value={form.giftMessage}
            maxLength={140}
            disabled={!giftEnabled}
            onChange={(event) => onChange({ giftMessage: event.target.value })}
            placeholder={giftEnabled ? 'Happy birthday, abuela!' : 'Enable "Gift wrap" to write a message'}
          />
        </label>
        <p className="mt-1 text-xs text-paper/50">{giftEnabled ? `${form.giftMessage.length}/140` : 'Gift wrap is off'}</p>
      </Section>

      <Section title="6. Quote summary">
        <div aria-live="polite" className="text-sm">
          {quoteLoading ? <p role="status" className="mb-2 text-paper/60">Loading...</p> : null}

          {quoteError ? (
            <p role="alert" className="mb-3 rounded-md border border-crimson/60 bg-crimson/15 px-3 py-2 text-paper">
              {quoteError}
            </p>
          ) : null}

          {quote ? (
            <>
              <ul className="space-y-1.5">
                {quote.layers.length === 0 ? (
                  <li className="text-paper/50">No extra layers selected</li>
                ) : (
                  quote.layers.map((layer) => (
                    <li key={layer.code} className="flex items-baseline justify-between gap-4 border-b border-dotted border-line pb-1.5">
                      <span className="text-paper/85">{layer.label}</span>
                      <span className="shrink-0 text-gold">{formatCop(layer.costCop)}</span>
                    </li>
                  ))
                )}
              </ul>
              <p className="mt-4 flex items-baseline justify-between gap-4">
                <span className="font-display text-lg font-semibold">Total</span>
                <span className="font-display text-2xl font-bold text-gold">{formatCop(quote.totalCop)}</span>
              </p>
            </>
          ) : !quoteLoading && !quoteError ? (
            <p className="text-paper/50">Choose a product to see the quote.</p>
          ) : null}
        </div>
      </Section>
    </div>
  )
}
