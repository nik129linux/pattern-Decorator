import QuoteWorkspace from './components/QuoteWorkspace'

export default function App() {
  return (
    <div className="mx-auto flex min-h-svh w-full max-w-[1400px] flex-col px-4 sm:px-8">
      <header className="flex flex-wrap items-baseline justify-between gap-3 border-b border-line py-5">
        <div>
          <h1 className="font-display text-3xl font-bold tracking-tight text-paper">
            Barniz <span className="text-gold">Express</span>
          </h1>
          <p className="text-sm text-paper/60">
            Layered shipping quotes for barniz de Pasto pieces
          </p>
        </div>
        <span className="rounded-full border border-gold/40 px-3 py-1 text-xs tracking-wide text-gold">
          Decorator pattern demo
        </span>
      </header>

      <QuoteWorkspace />

      <footer className="mt-auto border-t border-line py-4 text-xs text-paper/40">
        Handcrafted in Pasto, Nariño - quotes in Colombian pesos (COP)
      </footer>
    </div>
  )
}
