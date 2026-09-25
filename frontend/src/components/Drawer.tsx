import { useEffect, type FormEvent, type ReactNode } from 'react'

interface Props {
  titulo: string
  subtitulo?: string
  aberto: boolean
  salvando?: boolean
  textoSalvar?: string
  onFechar: () => void
  onSalvar: () => void
  children: ReactNode
}

export function Drawer({ titulo, subtitulo, aberto, salvando, textoSalvar = 'Salvar', onFechar, onSalvar, children }: Props) {
  useEffect(() => {
    if (!aberto) return
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && onFechar()
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [aberto, onFechar])

  if (!aberto) return null

  const submit = (e: FormEvent) => {
    e.preventDefault()
    onSalvar()
  }

  return (
    <div className="drawer-backdrop" onMouseDown={onFechar}>
      <form className="drawer" onMouseDown={(e) => e.stopPropagation()} onSubmit={submit}>
        <header className="drawer__head">
          <div>
            {subtitulo && <div className="eyebrow">{subtitulo}</div>}
            <h2>{titulo}</h2>
          </div>
          <button type="button" className="btn-text" onClick={onFechar} aria-label="Fechar">
            Esc
          </button>
        </header>
        <div className="drawer__body">{children}</div>
        <footer className="drawer__foot">
          <button type="button" className="btn btn--ghost" onClick={onFechar}>
            Cancelar
          </button>
          <button type="submit" className="btn btn--primary" disabled={salvando}>
            {salvando ? 'Salvando…' : textoSalvar}
          </button>
        </footer>
      </form>
    </div>
  )
}
