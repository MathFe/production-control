import { useCallback, useEffect, useRef, useState } from 'react'
import { ApiError } from './api'

/** Carrega dados da API e expõe um reload para depois de salvar algo. */
export function useData<T>(loader: () => Promise<T>) {
  const loaderRef = useRef(loader)
  const [versao, setVersao] = useState(0)
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Ignora respostas que chegam depois do componente sair da tela ou de um reload mais novo
    let ativo = true
    loaderRef
      .current()
      .then((d) => {
        if (!ativo) return
        setData(d)
        setError(null)
      })
      .catch((e) => ativo && setError(e instanceof ApiError ? e.message : 'Erro inesperado'))
      .finally(() => ativo && setLoading(false))
    return () => {
      ativo = false
    }
  }, [versao])

  const reload = useCallback(() => setVersao((v) => v + 1), [])

  return { data, error, loading, reload }
}
