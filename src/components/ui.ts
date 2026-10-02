const money = new Intl.NumberFormat('en-US')

/** 180000 -> "180,000 COP" */
export function formatCop(value: number): string {
  return `${money.format(Math.round(value))} COP`
}

export const inputClass =
  'w-full rounded-md border border-line bg-ink-soft px-3 py-2 text-paper placeholder:text-paper/40'

export const sectionTitleClass =
  'font-display text-lg font-semibold text-paper mb-3'

export const cardBase =
  'relative block cursor-pointer rounded-lg border bg-ink-soft/70 p-3 transition-colors'
