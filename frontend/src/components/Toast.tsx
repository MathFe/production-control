import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'

type Tipo = 'ok' | 'erro'
interface Aviso {
  id: number
  texto: string
  tipo: Tipo
}

const ToastContext = createContext<(texto: string, tipo?: Tipo) => void>(() => {})

let proximoId = 1

export function ToastProvider({ children }: { children: ReactNode }) {
  const [avisos, setAvisos] = useState<Aviso[]>([])

  const avisar = useCallback((texto: string, tipo: Tipo = 'ok') => {
    const id = proximoId++
    setAvisos((atual) => [...atual, { id, texto, tipo }])
    setTimeout(() => setAvisos((atual) => atual.filter((a) => a.id !== id)), tipo === 'erro' ? 6000 : 3000)
  }, [])

  return (
    <ToastContext.Provider value={avisar}>
      {children}
      <div className="toasts" role="status" aria-live="polite">
        {avisos.map((a) => (
          <div key={a.id} className={`toast toast--${a.tipo}`}>
            <span className="toast__tag">{a.tipo === 'erro' ? 'ERRO' : 'OK'}</span>
            {a.texto}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast() {
  return useContext(ToastContext)
}
