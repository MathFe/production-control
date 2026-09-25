import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react'
import { ApiError, api, encodeCredentials, setToken, setUnauthorizedHandler, type Usuario } from './api'

const USER_KEY = 'prensas.usuario'

interface AuthContextValue {
  usuario: Usuario | null
  entrar: (username: string, password: string) => Promise<void>
  cadastrar: (username: string, email: string, password: string) => Promise<void>
  sair: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

function lerUsuarioSalvo(): Usuario | null {
  try {
    const salvo = sessionStorage.getItem(USER_KEY)
    return salvo ? JSON.parse(salvo) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(lerUsuarioSalvo)

  const sair = useCallback(() => {
    setToken(null)
    sessionStorage.removeItem(USER_KEY)
    setUsuario(null)
  }, [])

  useEffect(() => setUnauthorizedHandler(sair), [sair])

  const entrar = useCallback(async (username: string, password: string) => {
    const token = encodeCredentials(username, password)
    let usuarios: Usuario[]
    try {
      usuarios = await api.usuarios.list(token)
    } catch (e) {
      if (e instanceof ApiError && e.status === 401) throw new ApiError(401, 'Usuário ou senha incorretos')
      throw e
    }

    const encontrado = usuarios.find((u) => u.username === username)
    if (!encontrado) throw new ApiError(404, 'Usuário não encontrado')

    setToken(token)
    sessionStorage.setItem(USER_KEY, JSON.stringify(encontrado))
    setUsuario(encontrado)
  }, [])

  const cadastrar = useCallback(
    async (username: string, email: string, password: string) => {
      await api.usuarios.create({ username, email, password })
      await entrar(username, password)
    },
    [entrar],
  )

  return <AuthContext.Provider value={{ usuario, entrar, cadastrar, sair }}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth precisa estar dentro de <AuthProvider>')
  return ctx
}
