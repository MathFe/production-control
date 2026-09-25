export type StatusCaixa = 'DISPONIVEL' | 'EM_PRODUCAO' | 'FINALIZADA'
export type StatusOrdem = 'PLANEJADA' | 'EM_PROCESSAMENTO' | 'CONCLUIDA'
export type NivelEstoque = 'ALTA' | 'MEDIA' | 'CRITICA'

export interface Usuario {
  id: number
  username: string
  email: string
  dataRegistro: string
}

export interface Caixa {
  id: number
  nome: string
  tamanho: string
  espessura: number
  quantidade: number
  dataEntrada: string
  status: StatusCaixa
}

export interface Estoque {
  id: number
  nomePeca: string
  quantidade: number
  quantidadeMinima: number
  nivelEstoque: NivelEstoque
  dataChegada: string
}

export interface Ordem {
  id: number
  caixaChapaId: number
  usuarioId: number
  estoqueProduzidoId: number | null
  idPrensa: number | null
  quantidadeAProcessar: number
  dataOrdem: string
  status: StatusOrdem
}

export class ApiError extends Error {
  readonly status: number
  readonly campos?: Record<string, string>

  constructor(status: number, mensagem: string, campos?: Record<string, string>) {
    super(mensagem)
    this.status = status
    this.campos = campos
  }
}

const TOKEN_KEY = 'prensas.token'

export function encodeCredentials(username: string, password: string) {
  const bytes = new TextEncoder().encode(`${username}:${password}`)
  return btoa(String.fromCharCode(...bytes))
}

export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string | null) {
  if (token) sessionStorage.setItem(TOKEN_KEY, token)
  else sessionStorage.removeItem(TOKEN_KEY)
}

let onUnauthorized: () => void = () => {}

export function setUnauthorizedHandler(handler: () => void) {
  onUnauthorized = handler
}

async function request<T>(method: string, path: string, body?: unknown, token = getToken()): Promise<T> {
  const headers: Record<string, string> = {
    // Faz o Spring Security responder 401 sem o header WWW-Authenticate,
    // senão o navegador abre aquele popup nativo de login
    'X-Requested-With': 'XMLHttpRequest',
  }
  if (token) headers.Authorization = `Basic ${token}`
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  let res: Response
  try {
    res = await fetch(`/api${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError(0, 'Sem conexão com o servidor')
  }

  if (res.status === 401) {
    if (token === getToken()) onUnauthorized()
    throw new ApiError(401, 'Sessão expirada ou credenciais inválidas')
  }

  if (!res.ok) {
    const erro = await res.json().catch(() => null)
    throw new ApiError(res.status, erro?.mensagem ?? `Erro ${res.status}`, erro?.campos)
  }

  return res.status === 204 ? (undefined as T) : res.json()
}

export const api = {
  usuarios: {
    list: (token?: string) => request<Usuario[]>('GET', '/usuarios', undefined, token),
    create: (body: { username: string; email: string; password: string }) =>
      request<Usuario>('POST', '/usuarios', body, null),
  },
  caixas: {
    list: () => request<Caixa[]>('GET', '/caixas-chapa'),
    create: (body: Pick<Caixa, 'nome' | 'tamanho' | 'espessura' | 'quantidade'>) =>
      request<Caixa>('POST', '/caixas-chapa', body),
    update: (id: number, body: Pick<Caixa, 'nome' | 'tamanho' | 'espessura' | 'quantidade' | 'status'>) =>
      request<Caixa>('PUT', `/caixas-chapa/${id}`, body),
    remove: (id: number) => request<void>('DELETE', `/caixas-chapa/${id}`),
  },
  estoques: {
    list: () => request<Estoque[]>('GET', '/estoques-produzidos'),
    create: (body: Pick<Estoque, 'nomePeca' | 'quantidade' | 'quantidadeMinima'>) =>
      request<Estoque>('POST', '/estoques-produzidos', body),
    update: (id: number, body: Pick<Estoque, 'nomePeca' | 'quantidade' | 'quantidadeMinima'>) =>
      request<Estoque>('PUT', `/estoques-produzidos/${id}`, body),
    remove: (id: number) => request<void>('DELETE', `/estoques-produzidos/${id}`),
  },
  ordens: {
    list: () => request<Ordem[]>('GET', '/ordens-producao'),
    create: (body: { caixaChapaId: number; usuarioId: number; quantidadeAProcessar: number }) =>
      request<Ordem>('POST', '/ordens-producao', body),
    update: (id: number, body: { idPrensa: number | null; quantidadeAProcessar: number; estoqueProduzidoId: number | null }) =>
      request<Ordem>('PUT', `/ordens-producao/${id}`, body),
    status: (id: number, status: StatusOrdem) =>
      request<Ordem>('PATCH', `/ordens-producao/${id}/status`, { status }),
    remove: (id: number) => request<void>('DELETE', `/ordens-producao/${id}`),
  },
}
