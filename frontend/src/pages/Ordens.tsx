import { useState } from 'react'
import { ApiError, api, type Ordem, type StatusOrdem } from '../api'
import { useAuth } from '../auth'
import { Drawer } from '../components/Drawer'
import { useToast } from '../components/Toast'
import { ConfirmButton, Empty, Field, PageHeader, Select, Status, Tabs } from '../components/ui'
import { fmt } from '../format'
import { useData } from '../hooks'

const carregar = () =>
  Promise.all([api.ordens.list(), api.caixas.list(), api.estoques.list(), api.usuarios.list()])

type Filtro = 'TODAS' | StatusOrdem

interface Form {
  id: number | null
  caixaChapaId: string
  quantidade: string
  estoqueId: string
  idPrensa: number | null
}

const VAZIO: Form = { id: null, caixaChapaId: '', quantidade: '', estoqueId: '', idPrensa: null }

export function Ordens() {
  const { usuario } = useAuth()
  const avisar = useToast()
  const { data, error, loading, reload } = useData(carregar)
  const [filtro, setFiltro] = useState<Filtro>('TODAS')
  const [form, setForm] = useState<Form | null>(null)
  const [campos, setCampos] = useState<Record<string, string>>({})
  const [salvando, setSalvando] = useState(false)
  const [ocupado, setOcupado] = useState<number | null>(null)

  if (error) return <Empty>{error}</Empty>
  if (loading && !data) return <div className="loading">Carregando…</div>
  if (!data) return null

  const [ordens, caixas, estoques, usuarios] = data
  const caixaPorId = new Map(caixas.map((c) => [c.id, c]))
  const estoquePorId = new Map(estoques.map((e) => [e.id, e]))
  const usuarioPorId = new Map(usuarios.map((u) => [u.id, u]))
  const disponiveis = caixas.filter((c) => c.status === 'DISPONIVEL')

  const contar = (s: StatusOrdem) => ordens.filter((o) => o.status === s).length
  const visiveis = ordens
    .filter((o) => filtro === 'TODAS' || o.status === filtro)
    .sort((a, b) => b.id - a.id)

  const abrirNova = () => {
    const primeira = disponiveis[0]
    setCampos({})
    setForm({ ...VAZIO, caixaChapaId: primeira ? String(primeira.id) : '', quantidade: primeira ? String(primeira.quantidade) : '' })
  }

  const abrirEdicao = (o: Ordem) => {
    setCampos({})
    setForm({
      id: o.id,
      caixaChapaId: String(o.caixaChapaId),
      quantidade: String(o.quantidadeAProcessar),
      estoqueId: o.estoqueProduzidoId ? String(o.estoqueProduzidoId) : '',
      idPrensa: o.idPrensa,
    })
  }

  const salvar = async () => {
    if (!form || !usuario) return
    setSalvando(true)
    setCampos({})
    const quantidadeAProcessar = Number(form.quantidade)
    const estoqueProduzidoId = form.estoqueId ? Number(form.estoqueId) : null
    try {
      if (form.id === null) {
        const criada = await api.ordens.create({
          caixaChapaId: Number(form.caixaChapaId),
          usuarioId: usuario.id,
          quantidadeAProcessar,
        })
        // O POST não recebe o estoque de destino; ele é vinculado logo em seguida
        if (estoqueProduzidoId) {
          await api.ordens.update(criada.id, { idPrensa: null, quantidadeAProcessar, estoqueProduzidoId })
        }
        avisar(`${fmt.op(criada.id)} criada`)
      } else {
        await api.ordens.update(form.id, { idPrensa: form.idPrensa, quantidadeAProcessar, estoqueProduzidoId })
        avisar(`${fmt.op(form.id)} atualizada`)
      }
      setForm(null)
      reload()
    } catch (e) {
      if (e instanceof ApiError) {
        setCampos(e.campos ?? {})
        avisar(e.message, 'erro')
      }
    } finally {
      setSalvando(false)
    }
  }

  const executar = async (o: Ordem, acao: () => Promise<unknown>, sucesso: string) => {
    setOcupado(o.id)
    try {
      await acao()
      avisar(sucesso)
      reload()
    } catch (e) {
      if (e instanceof ApiError) avisar(e.message, 'erro')
    } finally {
      setOcupado(null)
    }
  }

  const caixaSelecionada = form ? caixaPorId.get(Number(form.caixaChapaId)) : undefined

  return (
    <>
      <PageHeader indice="02 — Ordens de produção" titulo="Ordens de produção">
        <button className="btn btn--primary" onClick={abrirNova} disabled={disponiveis.length === 0}>
          Nova ordem
        </button>
      </PageHeader>

      {disponiveis.length === 0 && (
        <p className="note">Nenhuma caixa de chapa disponível. Cadastre uma caixa antes de abrir uma ordem.</p>
      )}

      <Tabs
        valor={filtro}
        onChange={setFiltro}
        opcoes={[
          { valor: 'TODAS', rotulo: 'Todas', qtd: ordens.length },
          { valor: 'PLANEJADA', rotulo: 'Planejadas', qtd: contar('PLANEJADA') },
          { valor: 'EM_PROCESSAMENTO', rotulo: 'Na prensa', qtd: contar('EM_PROCESSAMENTO') },
          { valor: 'CONCLUIDA', rotulo: 'Concluídas', qtd: contar('CONCLUIDA') },
        ]}
      />

      {visiveis.length === 0 ? (
        <Empty>Nenhuma ordem {filtro !== 'TODAS' && 'neste status'}.</Empty>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Ordem</th>
                <th>Caixa de chapa</th>
                <th className="num">Qtd.</th>
                <th>Destino</th>
                <th>Operador</th>
                <th>Aberta em</th>
                <th>Status</th>
                <th className="actions" aria-label="Ações" />
              </tr>
            </thead>
            <tbody>
              {visiveis.map((o) => {
                const caixa = caixaPorId.get(o.caixaChapaId)
                const destino = o.estoqueProduzidoId ? estoquePorId.get(o.estoqueProduzidoId) : undefined
                const semDestino = !o.estoqueProduzidoId
                return (
                  <tr key={o.id} className={ocupado === o.id ? 'is-busy' : undefined}>
                    <td className="mono">{fmt.op(o.id)}</td>
                    <td>
                      {caixa?.nome ?? '—'}
                      <span className="sub mono">{fmt.cx(o.caixaChapaId)}</span>
                    </td>
                    <td className="num mono">{fmt.n(o.quantidadeAProcessar)}</td>
                    <td>{destino ? destino.nomePeca : <span className="warn-text">Sem destino</span>}</td>
                    <td className="dim">{usuarioPorId.get(o.usuarioId)?.username ?? '—'}</td>
                    <td className="mono dim">{fmt.dataHora(o.dataOrdem)}</td>
                    <td>
                      <Status valor={o.status} />
                    </td>
                    <td className="actions">
                      {o.status === 'PLANEJADA' && (
                        <>
                          <button className="btn-text" onClick={() => abrirEdicao(o)}>
                            Editar
                          </button>
                          <ConfirmButton
                            onConfirm={() => executar(o, () => api.ordens.remove(o.id), `${fmt.op(o.id)} excluída`)}
                          />
                          <button
                            className="btn btn--sm"
                            disabled={semDestino}
                            title={semDestino ? 'Defina o estoque de destino antes de iniciar' : undefined}
                            onClick={() =>
                              executar(o, () => api.ordens.status(o.id, 'EM_PROCESSAMENTO'), `${fmt.op(o.id)} na prensa`)
                            }
                          >
                            Iniciar
                          </button>
                        </>
                      )}
                      {o.status === 'EM_PROCESSAMENTO' && (
                        <button
                          className="btn btn--sm btn--primary"
                          disabled={semDestino}
                          title={semDestino ? 'Ordem sem estoque de destino não pode ser concluída' : undefined}
                          onClick={() =>
                            executar(
                              o,
                              () => api.ordens.status(o.id, 'CONCLUIDA'),
                              `${fmt.op(o.id)} concluída · +${fmt.n(o.quantidadeAProcessar)} em ${destino?.nomePeca ?? 'estoque'}`,
                            )
                          }
                        >
                          Concluir
                        </button>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}

      <Drawer
        aberto={form !== null}
        titulo={form?.id ? fmt.op(form.id) : 'Nova ordem'}
        subtitulo={form?.id ? 'Editar ordem' : 'Ordem de produção'}
        textoSalvar={form?.id ? 'Salvar' : 'Abrir ordem'}
        salvando={salvando}
        onFechar={() => setForm(null)}
        onSalvar={salvar}
      >
        {form && (
          <>
            {form.id === null ? (
              <Select
                label="Caixa de chapa"
                value={form.caixaChapaId}
                onChange={(v) => setForm({ ...form, caixaChapaId: v, quantidade: String(caixaPorId.get(Number(v))?.quantidade ?? '') })}
                erro={campos.caixaChapaId}
                required
              >
                {disponiveis.map((c) => (
                  <option key={c.id} value={c.id}>
                    {fmt.cx(c.id)} · {c.nome} · {fmt.mm(c.espessura)}
                  </option>
                ))}
              </Select>
            ) : (
              <div className="readonly">
                <span className="field__label">Caixa de chapa</span>
                {fmt.cx(Number(form.caixaChapaId))} · {caixaSelecionada?.nome}
              </div>
            )}

            <Field
              label="Quantidade a processar"
              type="number"
              min={1}
              max={caixaSelecionada?.quantidade}
              value={form.quantidade}
              onChange={(e) => setForm({ ...form, quantidade: e.target.value })}
              erro={campos.quantidadeAProcessar}
              dica={caixaSelecionada && `A caixa tem ${fmt.n(caixaSelecionada.quantidade)} chapas`}
              required
            />

            <Select
              label="Estoque de destino"
              value={form.estoqueId}
              onChange={(v) => setForm({ ...form, estoqueId: v })}
              dica="Obrigatório para iniciar a ordem na prensa"
            >
              <option value="">Definir depois</option>
              {estoques.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.nomePeca} · {fmt.n(e.quantidade)} em estoque
                </option>
              ))}
            </Select>
          </>
        )}
      </Drawer>
    </>
  )
}
