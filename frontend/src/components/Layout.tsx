import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth'
import { fmt } from '../format'

const NAV = [
  { to: '/', rotulo: 'Painel', n: '01' },
  { to: '/ordens', rotulo: 'Ordens de produção', n: '02' },
  { to: '/chapas', rotulo: 'Caixas de chapa', n: '03' },
  { to: '/estoque', rotulo: 'Estoque de peças', n: '04' },
]

export function Layout() {
  const { usuario, sair } = useAuth()

  return (
    <div className="shell">
      <aside className="rail">
        <div className="brand">
          <span className="brand__mark" aria-hidden />
          <div>
            <div className="brand__name">Prensas</div>
            <div className="brand__sub">Linha de estampagem</div>
          </div>
        </div>

        <nav className="nav">
          {NAV.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === '/'} className="nav__item">
              <span className="nav__n">{item.n}</span>
              {item.rotulo}
            </NavLink>
          ))}
        </nav>

        <div className="rail__foot">
          <div className="rail__date">{fmt.hoje()}</div>
          <div className="rail__user">
            <span className="rail__avatar">{usuario?.username.slice(0, 2).toUpperCase()}</span>
            <div className="rail__who">
              <strong>{usuario?.username}</strong>
              <button className="btn-text" onClick={sair}>
                Sair
              </button>
            </div>
          </div>
        </div>
      </aside>

      <main className="main">
        <Outlet />
      </main>
    </div>
  )
}
