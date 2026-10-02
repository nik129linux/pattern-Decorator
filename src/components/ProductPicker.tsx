import type { Product } from '../types/api'
import { cardBase, formatCop } from './ui'

interface ProductPickerProps {
  products: Product[]
  selectedId: string
  onChange: (productId: string) => void
}

export default function ProductPicker({ products, selectedId, onChange }: ProductPickerProps) {
  if (products.length === 0) {
    return <p className="text-sm text-paper/60">Loading...</p>
  }

  return (
    <div role="radiogroup" aria-label="Product" className="grid gap-3 sm:grid-cols-2">
      {products.map((product) => {
        const active = product.id === selectedId
        return (
          <label key={product.id} className="relative">
            <input
              type="radio"
              name="product"
              value={product.id}
              checked={active}
              onChange={() => onChange(product.id)}
              className="peer sr-only"
            />
            <span
              className={`${cardBase} ${
                active
                  ? 'border-gold bg-gold/10'
                  : 'border-line hover:border-gold/50 peer-focus-visible:ring-2 peer-focus-visible:ring-gold'
              }`}
            >
              <span className="block font-display text-[15px] font-semibold text-paper">
                {product.name}
              </span>
              <span className="mt-0.5 block text-sm text-gold">{formatCop(product.basePriceCop)}</span>
              <span className="mt-1 block text-xs leading-snug text-paper/50">{product.description}</span>
            </span>
          </label>
        )
      })}
    </div>
  )
}
