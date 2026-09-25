import { useState } from 'react'
import { ApiError, api, type Estoque as Item, type NivelEstoque } from '../api'
import { Drawer } from '../components/Drawer'
import { useToast } from '../components/Toast'
import { ConfirmButton, Empty, Field, LevelBar, PageHeader, Status, Tabs } from '../components/ui'
import { fmt } from '../format'
import { useData } from '../hooks'

type Filtro = 'TODOS' | NivelEstoque

interface Form {
  id: number | null
  nomePeca: string
  quantidade: string
  quantidadeMinima: string
}

const VAZIO: Form = { id: null, nomePeca: '', quantidade: '0', quantidadeMinima: '' }

export function Estoque() {
  const avisar = useToast()
  const { data: itens, error, loading, reload } = useData(api.estoques.list)
  const [filtro, setFiltro] = useState<Filtro>('TODOS')
  const [form, setForm] = useState<Form | null>(null)
  const [campos, setCampos] = useState<Record<string, string>>({})
  const [salvando, setSalvando] = useState(false)

  if (error) return <Empty>{error}</Empty>
  if (loading && !itens) return <div className="loading">Carregando…</div>
  if (!itens) return null

  const contar = (n: NivelEstoque) => itens.filter((i) => i.nivelEstoque === n).length
  const visiveis = itens
    .filter((i) => filtro === 'TODOS' || i.nivelEstoque === filtro)
    .sort((a, b) => a.nomePeca.localeCompare(b.nomePeca, 'pt-BR'))

  const abrir = (i?: Item) => {
    setCampos({})
    setForm(
      i
        ? { id: i.id, nomePeca: i.nomePeca, quantidade: String(i.quantidade), quantidadeMinima: String(i.quantidadeMinima) }
        : VAZIO,
    )
  }

  const salvar = async () => {
    if (!form) return
    setSalvando(true)
    setCampos({})
    const body = {
      nomePeca: form.nomePeca.trim(),
      quantidade: Number(form.quantidade),
      quantidadeMinima: Number(form.quantidadeMinima),
    }
    try {
      if (form.id === null) await api.estoques.create(body)
      else await api.estoques.update(form.id, body)
      avisar(`${body.nomePeca} ${form.id === null ? 'cadastrada' : 'atualizada'}`)
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

  const excluir = async (i: Item) => {
    try {
      await api.estoques.remove(i.id)
      avisar(`${i.nomePeca} excluída`)
      reload()
    } catch (e) {
      if (e instanceof ApiError) avisar(e.status === 409 ? 'Há ordens de produção usando este item' : e.message, 'erro')
    }
  }

  return (
    <>
      <PageHeader indice="04 — Estoque de peças" titulo="Estoque de peças">
        <button className="btn btn--primary" onClick={() => abrir()}>
          Nova peça
        </button>
      </PageHeader>

      <Tabs
        valor={filtro}
        onChange={setFiltro}
        opcoes={[
          { valor: 'TODOS', rotulo: 'Todos', qtd: itens.length },
          { valor: 'CRITICA', rotulo: 'Críticos', qtd: contar('CRITICA') },
          { valor: 'MEDIA', rotulo: 'Médios', qtd: contar('MEDIA') },
          { valor: 'ALTA', rotulo: 'Altos', qtd: contar('ALTA') },
        ]}
      />

      {visiveis.length === 0 ? (
        <Empty>Nenhuma peça {filtro !== 'TODOS' && 'neste nível'}.</Empty>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Peça</th>
                <th className="num">Em estoque</th>
                <th className="num">Mínimo</th>
                <th className="col-level">Nível</th>
                <th>Status</th>
                <th>Cadastro</th>
                <th className="actions" aria-label="Ações" />
              </tr>
            </thead>
            <tbody>
              {visiveis.map((i) => (
                <tr key={i.id}>
                  <td>{i.nomePeca}</td>
                  <td className="num mono">{fmt.n(i.quantidade)}</td>
                  <td className="num mono dim">{fmt.n(i.quantidadeMinima)}</td>
                  <td className="col-level">
                    <LevelBar quantidade={i.quantidade} minimo={i.quantidadeMinima} nivel={i.nivelEstoque} />
                  </td>
                  <td>
                    <Status valor={i.nivelEstoque} />
                  </td>
                  <td className="mono dim">{fmt.data(i.dataChegada)}</td>
                  <td className="actions">
                    <button className="btn-text" onClick={() => abrir(i)}>
                      Editar
                    </button>
                    <ConfirmButton onConfirm={() => excluir(i)} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <p className="note note--legend">
        O nível é calculado pela API: <strong>crítico</strong> até o mínimo, <strong>médio</strong> até 2× o mínimo,{' '}
        <strong>alto</strong> acima disso. As marcas na barra mostram esses limites.
      </p>

      <Drawer
        aberto={form !== null}
        titulo={form?.id ? form.nomePeca || 'Peça' : 'Nova peça'}
        subtitulo={form?.id ? 'Editar peça' : 'Estoque de peças'}
        salvando={salvando}
        onFechar={() => setForm(null)}
        onSalvar={salvar}
      >
        {form && (
          <>
            <Field
              label="Nome da peça"
              value={form.nomePeca}
              onChange={(e) => setForm({ ...form, nomePeca: e.target.value })}
              erro={campos.nomePeca}
              placeholder="Ex.: Painel porta dianteira LE"
              maxLength={150}
              required
              autoFocus
            />
            <div className="field-row">
              <Field
                label="Quantidade atual"
                type="number"
                min={0}
                value={form.quantidade}
                onChange={(e) => setForm({ ...form, quantidade: e.target.value })}
                erro={campos.quantidade}
                dica={form.id ? 'Ajuste manual; ordens concluídas somam sozinhas' : undefined}
                required
              />
              <Field
                label="Estoque mínimo"
                type="number"
                min={0}
                value={form.quantidadeMinima}
                onChange={(e) => setForm({ ...form, quantidadeMinima: e.target.value })}
                erro={campos.quantidadeMinima}
                required
              />
            </div>
          </>
        )}
      </Drawer>
    </>
  )
}
