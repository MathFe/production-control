import { Link } from 'react-router-dom'
import { api } from '../api'
import { Empty, LevelBar, PageHeader, Status } from '../components/ui'
import { fmt } from '../format'
import { useData } from '../hooks'

const carregar = () => Promise.all([api.caixas.list(), api.ordens.list(), api.estoques.list()])

export function Painel() {
  const { data, error, loading } = useData(carregar)

  if (error) return <Empty>{error}</Empty>
  if (loading && !data) return <div className="loading">Carregando…</div>
  if (!data) return null

  const [caixas, ordens, estoques] = data
  const caixaPorId = new Map(caixas.map((c) => [c.id, c]))

  const disponiveis = caixas.filter((c) => c.status === 'DISPONIVEL')
  const planejadas = ordens.filter((o) => o.status === 'PLANEJADA')
  const naPrensa = ordens.filter((o) => o.status === 'EM_PROCESSAMENTO')
  const concluidas = ordens.filter((o) => o.status === 'CONCLUIDA')
  const soma = (xs: { quantidade?: number; quantidadeAProcessar?: number }[]) =>
    xs.reduce((t, x) => t + (x.quantidade ?? x.quantidadeAProcessar ?? 0), 0)

  const emAndamento = [...naPrensa, ...planejadas]
  const atencao = estoques
    .filter((e) => e.nivelEstoque !== 'ALTA')
    .sort((a, b) => a.quantidade / Math.max(a.quantidadeMinima, 1) - b.quantidade / Math.max(b.quantidadeMinima, 1))

  const etapas = [
    { rotulo: 'Chapas disponíveis', valor: soma(disponiveis), detalhe: `${disponiveis.length} caixas`, to: '/chapas' },
    { rotulo: 'Aguardando prensa', valor: soma(planejadas), detalhe: `${planejadas.length} ordens`, to: '/ordens' },
    { rotulo: 'Na prensa agora', valor: soma(naPrensa), detalhe: `${naPrensa.length} ordens`, to: '/ordens', ativo: naPrensa.length > 0 },
    { rotulo: 'Peças em estoque', valor: soma(estoques), detalhe: `${estoques.length} itens`, to: '/estoque' },
  ]

  return (
    <>
      <PageHeader indice="01 — Painel" titulo="Situação da linha" />

      <section className="pipeline">
        {etapas.map((e) => (
          <Link key={e.rotulo} to={e.to} className={`stage${e.ativo ? ' stage--live' : ''}`}>
            <span className="stage__label">{e.rotulo}</span>
            <span className="stage__value">{fmt.n(e.valor)}</span>
            <span className="stage__detail">{e.detalhe}</span>
          </Link>
        ))}
      </section>

      <div className="grid-2">
        <section className="panel">
          <header className="panel__head">
            <h3>Em andamento</h3>
            <Link to="/ordens" className="btn-text">
              Ver ordens →
            </Link>
          </header>
          {emAndamento.length === 0 ? (
            <Empty>Nenhuma ordem aberta. {concluidas.length > 0 && `${concluidas.length} já concluídas.`}</Empty>
          ) : (
            <ul className="list">
              {emAndamento.slice(0, 8).map((o) => (
                <li key={o.id} className="list__row">
                  <span className="mono dim">{fmt.op(o.id)}</span>
                  <span className="list__main">{caixaPorId.get(o.caixaChapaId)?.nome ?? fmt.cx(o.caixaChapaId)}</span>
                  <span className="mono">{fmt.n(o.quantidadeAProcessar)} un</span>
                  <Status valor={o.status} />
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="panel">
          <header className="panel__head">
            <h3>Estoque pedindo atenção</h3>
            <Link to="/estoque" className="btn-text">
              Ver estoque →
            </Link>
          </header>
          {atencao.length === 0 ? (
            <Empty>Todos os itens acima de 2× o mínimo.</Empty>
          ) : (
            <ul className="list">
              {atencao.slice(0, 8).map((e) => (
                <li key={e.id} className="list__row list__row--stock">
                  <span className="list__main">{e.nomePeca}</span>
                  <LevelBar quantidade={e.quantidade} minimo={e.quantidadeMinima} nivel={e.nivelEstoque} />
                  <span className="mono">
                    {fmt.n(e.quantidade)}
                    <span className="dim"> / {fmt.n(e.quantidadeMinima)}</span>
                  </span>
                  <Status valor={e.nivelEstoque} />
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </>
  )
}
