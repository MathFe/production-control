import { useState, type FormEvent } from 'react'
import { ApiError } from '../api'
import { useAuth } from '../auth'
import { Field } from '../components/ui'

export function Login() {
  const { entrar, cadastrar } = useAuth()
  const [modo, setModo] = useState<'entrar' | 'cadastrar'>('entrar')
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [campos, setCampos] = useState<Record<string, string>>({})
  const [enviando, setEnviando] = useState(false)

  const submit = async (e: FormEvent) => {
    e.preventDefault()
    setEnviando(true)
    setErro(null)
    setCampos({})
    try {
      if (modo === 'entrar') await entrar(username.trim(), password)
      else await cadastrar(username.trim(), email.trim(), password)
    } catch (e) {
      if (e instanceof ApiError) {
        setErro(e.status === 409 ? 'Esse usuário ou e-mail já está cadastrado' : e.message)
        setCampos(e.campos ?? {})
      } else setErro('Erro inesperado')
    } finally {
      setEnviando(false)
    }
  }

  const trocar = () => {
    setModo(modo === 'entrar' ? 'cadastrar' : 'entrar')
    setErro(null)
    setCampos({})
  }

  return (
    <div className="login">
      <section className="login__intro">
        <div className="brand">
          <span className="brand__mark" aria-hidden />
          <div>
            <div className="brand__name">Prensas</div>
            <div className="brand__sub">Linha de estampagem</div>
          </div>
        </div>

        <h1 className="login__title">
          Da chapa lisa
          <br />à peça no estoque.
        </h1>

        <ol className="flow">
          <li>
            <span>01</span>Caixa de chapa
          </li>
          <li>
            <span>02</span>Ordem de produção
          </li>
          <li>
            <span>03</span>Prensa
          </li>
          <li>
            <span>04</span>Estoque de peças
          </li>
        </ol>
      </section>

      <section className="login__panel">
        <form className="login__form" onSubmit={submit}>
          <div className="eyebrow">{modo === 'entrar' ? 'Acesso' : 'Novo operador'}</div>
          <h2>{modo === 'entrar' ? 'Entrar' : 'Criar conta'}</h2>

          <Field
            label="Usuário"
            autoComplete="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            erro={campos.username}
            required
            autoFocus
          />
          {modo === 'cadastrar' && (
            <Field
              label="E-mail"
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              erro={campos.email}
              required
            />
          )}
          <Field
            label="Senha"
            type="password"
            autoComplete={modo === 'entrar' ? 'current-password' : 'new-password'}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            erro={campos.password}
            dica={modo === 'cadastrar' ? 'Mínimo de 8 caracteres' : undefined}
            minLength={modo === 'cadastrar' ? 8 : undefined}
            required
          />

          {erro && <p className="form-error">{erro}</p>}

          <button type="submit" className="btn btn--primary btn--block" disabled={enviando}>
            {enviando ? 'Aguarde…' : modo === 'entrar' ? 'Entrar' : 'Criar conta e entrar'}
          </button>

          <p className="login__switch">
            {modo === 'entrar' ? 'Primeiro acesso?' : 'Já tem conta?'}{' '}
            <button type="button" className="btn-text btn-text--accent" onClick={trocar}>
              {modo === 'entrar' ? 'Criar conta' : 'Entrar'}
            </button>
          </p>
        </form>
      </section>
    </div>
  )
}
