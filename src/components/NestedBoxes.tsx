import type { ReactNode } from 'react'
import type { Product, QuoteLayer } from '../types/api'
import { formatCop } from './ui'

/** Border color per decorator layer code. */
const LAYER_COLORS: Record<string, string> = {
  FRAGILE: '#8a8178',
  INSURANCE: '#c9a24a',
  CUSTOMS: '#f4ece0',
  GIFT: '#8c1c13',
  EXPRESS: '#2f6b4f',
}

interface NestedBoxesProps {
  product?: Product
  layers: QuoteLayer[]
  loading: boolean
  error: string | null
}

/**
 * Plain-CSS "Russian dolls": the product box wrapped by one bordered div
 * per quote layer, in API order (innermost to outermost).
 */
export default function NestedBoxes({ product, layers, loading, error }: NestedBoxesProps) {
  const center: ReactNode = (
    <div className="rounded-md border-2 border-gold bg-gradient-to-b from-ink-soft to-ink px-8 py-7 text-center shadow-[0_10px_40px_-18px_rgba(0,0,0,0.9)]">
      <p className="mb-1 text-[11px] uppercase tracking-[0.2em] text-gold/80">Your package</p>
      <h3 className="font-display text-xl font-semibold text-paper">
        {product ? product.name : 'No product selected'}
      </h3>
      {product ? (
        <p className="mt-1 text-sm text-paper/60">
          {formatCop(product.basePriceCop)} - {product.weightKg} kg
        </p>
      ) : null}
    </div>
  )

  // Outermost element must be the first nested wrapper in JSX.
  const nested = [...layers].reverse().reduce<ReactNode>((child, layer) => {
    const color = LAYER_COLORS[layer.code] ?? '#f4ece0'
    return (
      <div
        key={layer.code}
        className="layer-wrap relative rounded-xl border-2 p-4 sm:p-5"
        style={{ borderColor: color }}
      >
        <span
          className="absolute -top-3 left-4 rounded-full bg-ink px-2 py-0.5 text-[11px] font-medium tracking-wide"
          style={{ color }}
        >
          {layer.label} - {formatCop(layer.costCop)}
        </span>
        {child}
      </div>
    )
  }, center)

  return (
    <section
      aria-label="Package wrapping preview"
      className="flex min-h-[45vh] flex-col items-center justify-center rounded-2xl border border-line bg-ink-soft/40 p-6 sm:min-h-[70vh]"
    >
      <div aria-live="polite" className="flex w-full items-center justify-center">
        {nested}
      </div>

      <p className="mt-6 h-5 text-sm text-paper/60" role="status">
        {loading ? 'Loading...' : null}
      </p>
      {error ? (
        <p role="alert" className="rounded-md border border-crimson/60 bg-crimson/15 px-3 py-2 text-sm text-paper">
          {error}
        </p>
      ) : null}
    </section>
  )
}
