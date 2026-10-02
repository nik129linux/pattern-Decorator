import type { ShippingOption } from '../types/api'
import { cardBase } from './ui'

interface OptionTogglesProps {
  options: ShippingOption[]
  selected: string[]
  onToggle: (code: string) => void
}

export default function OptionToggles({ options, selected, onToggle }: OptionTogglesProps) {
  if (options.length === 0) {
    return <p className="text-sm text-paper/60">Loading...</p>
  }

  return (
    <div className="grid gap-3">
      {options.map((option) => {
        const active = selected.includes(option.code)
        // Selected options stay toggleable so a stale pair can always be cleared.
        const disabled = !active && option.incompatibleWith.some((code) => selected.includes(code))
        const activeColor =
          option.code === 'INSURANCE'
            ? 'border-gold bg-gold/10'
            : option.code === 'GIFT'
              ? 'border-crimson bg-crimson/10'
              : option.code === 'EXPRESS'
                ? 'border-moss bg-moss/10'
                : 'border-paper/60 bg-paper/5'

        return (
          <label key={option.code} className={`relative ${disabled ? 'cursor-not-allowed' : ''}`}>
            <input
              type="checkbox"
              value={option.code}
              checked={active}
              disabled={disabled}
              onChange={() => onToggle(option.code)}
              className="peer sr-only"
            />
            <span
              className={`${cardBase} flex items-start gap-3 ${
                disabled
                  ? 'border-line/60 opacity-50'
                  : active
                    ? activeColor
                    : 'border-line hover:border-gold/50 peer-focus-visible:ring-2 peer-focus-visible:ring-gold'
              }`}
            >
              <span
                aria-hidden="true"
                className={`mt-0.5 grid size-5 shrink-0 place-items-center rounded border text-xs ${
                  active ? 'border-gold bg-gold text-ink' : 'border-paper/40 text-transparent'
                }`}
              >
                ✓
              </span>
              <span className="min-w-0">
                <span className="flex flex-wrap items-baseline gap-x-2">
                  <span className="font-display text-[15px] font-semibold text-paper">{option.name}</span>
                  <span className="rounded-full border border-line px-2 py-px text-[11px] text-gold">
                    {option.pricingRule}
                  </span>
                </span>
                <span className="mt-0.5 block text-xs leading-snug text-paper/60">
                  {option.description}
                </span>
                {disabled ? (
                  <span className="mt-1 block text-[11px] text-crimson">
                    Incompatible with a selected option
                  </span>
                ) : null}
              </span>
            </span>
          </label>
        )
      })}
    </div>
  )
}
