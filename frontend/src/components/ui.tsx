import { useEffect, useState, type InputHTMLAttributes, type ReactNode } from 'react'
import type { NivelEstoque, StatusCaixa, StatusOrdem } from '../api'

const ROTULOS: Record<StatusCaixa | StatusOrdem | NivelEstoque, [string, string]> = {
  DISPONIVEL: ['Disponível', 'ok'],
  EM_PRODUCAO: ['Em produção', 'run'],
  FINALIZADA: ['Finalizada', 'off'],
  PLANEJADA: ['Planejada', 'idle'],
  EM_PROCESSAMENTO: ['Na prensa', 'run'],
  CONCLUIDA: ['Concluída', 'ok'],
  ALTA: ['Alta', 'ok'],
  MEDIA: ['Média', 'warn'],
  CRITICA: ['Crítica', 'crit'],
}

export function Status({ valor }: { valor: keyof typeof ROTULOS }) {
  const [texto, tom] = ROTULOS[valor]
  return (
    <span className={`status status--${tom}`}>
      <i />
      {texto}
    </span>
  )
}

interface FieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  erro?: string
  dica?: ReactNode
}

export function Field({ label, erro, dica, ...input }: FieldProps) {
  return (
    <label className={`field${erro ? ' field--erro' : ''}`}>
      <span className="field__label">{label}</span>
      <input {...input} />
      {erro ? <span className="field__msg">{erro}</span> : dica && <span className="field__hint">{dica}</span>}
    </label>
  )
}

interface SelectProps {
  label: string
  value: string
  onChange: (v: string) => void
  children: ReactNode
  erro?: string
  dica?: ReactNode
  required?: boolean
}

export function Select({ label, value, onChange, children, erro, dica, required }: SelectProps) {
  return (
    <label className={`field${erro ? ' field--erro' : ''}`}>
      <span className="field__label">{label}</span>
      <select value={value} onChange={(e) => onChange(e.target.value)} required={required}>
        {children}
      </select>
      {erro ? <span className="field__msg">{erro}</span> : dica && <span className="field__hint">{dica}</span>}
    </label>
  )
}

/** Botão de exclusão em dois cliques, sem popup: o primeiro arma, o segundo confirma. */
export function ConfirmButton({ onConfirm, children = 'Excluir' }: { onConfirm: () => void; children?: ReactNode }) {
  const [armado, setArmado] = useState(false)

  useEffect(() => {
    if (!armado) return
    const t = setTimeout(() => setArmado(false), 3000)
    return () => clearTimeout(t)
  }, [armado])

  return (
    <button
      type="button"
      className={`btn-text btn-text--danger${armado ? ' is-armed' : ''}`}
      onClick={() => (armado ? (setArmado(false), onConfirm()) : setArmado(true))}
    >
      {armado ? 'Confirmar?' : children}
    </button>
  )
}

/** Barra de nível: escala vai até 3× o mínimo, com marcas no mínimo e em 2×. */
export function LevelBar({ quantidade, minimo, nivel }: { quantidade: number; minimo: number; nivel: NivelEstoque }) {
  const escala = Math.max(minimo * 3, quantidade, 1)
  const pct = Math.min(100, (quantidade / escala) * 100)
  return (
    <div className={`level level--${nivel.toLowerCase()}`} title={`${quantidade} / mínimo ${minimo}`}>
      <div className="level__fill" style={{ width: `${pct}%` }} />
      {minimo > 0 && (
        <>
          <span className="level__tick" style={{ left: `${(minimo / escala) * 100}%` }} />
          <span className="level__tick" style={{ left: `${((minimo * 2) / escala) * 100}%` }} />
        </>
      )}
    </div>
  )
}

export function Tabs<T extends string>({
  valor,
  onChange,
  opcoes,
}: {
  valor: T
  onChange: (v: T) => void
  opcoes: { valor: T; rotulo: string; qtd: number }[]
}) {
  return (
    <div className="tabs" role="tablist">
      {opcoes.map((o) => (
        <button
          key={o.valor}
          role="tab"
          aria-selected={valor === o.valor}
          className={`tab${valor === o.valor ? ' is-active' : ''}`}
          onClick={() => onChange(o.valor)}
        >
          {o.rotulo}
          <span className="tab__n">{o.qtd}</span>
        </button>
      ))}
    </div>
  )
}

export function PageHeader({ indice, titulo, children }: { indice: string; titulo: string; children?: ReactNode }) {
  return (
    <header className="page-head">
      <div>
        <div className="eyebrow">{indice}</div>
        <h1>{titulo}</h1>
      </div>
      {children && <div className="page-head__actions">{children}</div>}
    </header>
  )
}

export function Empty({ children }: { children: ReactNode }) {
  return <div className="empty">{children}</div>
}
