import { useState } from 'react'
import { ApiError, api, type Caixa, type StatusCaixa } from '../api'
import { Drawer } from '../components/Drawer'
import { useToast } from '../components/Toast'
import { ConfirmButton, Empty, Field, PageHeader, Status, Tabs } from '../components/ui'
import { fmt } from '../format'
import { useData } from '../hooks'

type Filtro = 'TODAS' | StatusCaixa

interface Form {
  id: number | null
  status: StatusCaixa
  nome: string
  tamanho: string
  espessura: string
  quantidade: string
}

const VAZIO: Form = { id: null, status: 'DISPONIVEL', nome: '', tamanho: '', espessura: '', quantidade: '' }

export function Chapas() {
  const avisar = useToast()
  const { data: caixas, error, loading, reload } = useData(api.caixas.list)
  const [filtro, setFiltro] = useState<Filtro>('TODAS')
  const [form, setForm] = useState<Form | null>(null)
  const [campos, setCampos] = useState<Record<string, string>>({})
  const [salvando, setSalvando] = useState(false)

  if (error) return <Empty>{error}</Empty>
  if (loading && !caixas) return <div className="loading">Carregando…</div>
  if (!caixas) return null

  const contar = (s: StatusCaixa) => caixas.filter((c) => c.status === s).length
  const visiveis = caixas.filter((c) => filtro === 'TODAS' || c.status === filtro).sort((a, b) => b.id - a.id)

  const abrir = (c?: Caixa) => {
    setCampos({})
    setForm(
      c
        ? { id: c.id, status: c.status, nome: c.nome, tamanho: c.tamanho, espessura: String(c.espessura), quantidade: String(c.quantidade) }
        : VAZIO,
    )
  }

  const salvar = async () => {
    if (!form) return
    setSalvando(true)
    setCampos({})
    const body = {
      nome: form.nome.trim(),
      tamanho: form.tamanho.trim(),
      espessura: Number(form.espessura.replace(',', '.')),
      quantidade: Number(form.quantidade),
    }
    try {
      if (form.id === null) {
        const criada = await api.caixas.create(body)
        avisar(`${fmt.cx(criada.id)} cadastrada`)
      } else {
        // O status da caixa é controlado pelas ordens; aqui ele só é repassado
        await api.caixas.update(form.id, { ...body, status: form.status })
        avisar(`${fmt.cx(form.id)} atualizada`)
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

  const excluir = async (c: Caixa) => {
    try {
      await api.caixas.remove(c.id)
      avisar(`${fmt.cx(c.id)} excluída`)
      reload()
    } catch (e) {
      if (e instanceof ApiError) avisar(e.status === 409 ? 'A caixa tem ordem de produção vinculada' : e.message, 'erro')
    }
  }

  return (
    <>
      <PageHeader indice="03 — Caixas de chapa" titulo="Caixas de chapa">
        <button className="btn btn--primary" onClick={() => abrir()}>
          Receber caixa
        </button>
      </PageHeader>

      <Tabs
        valor={filtro}
        onChange={setFiltro}
        opcoes={[
          { valor: 'TODAS', rotulo: 'Todas', qtd: caixas.length },
          { valor: 'DISPONIVEL', rotulo: 'Disponíveis', qtd: contar('DISPONIVEL') },
          { valor: 'EM_PRODUCAO', rotulo: 'Em produção', qtd: contar('EM_PRODUCAO') },
          { valor: 'FINALIZADA', rotulo: 'Finalizadas', qtd: contar('FINALIZADA') },
        ]}
      />

      {visiveis.length === 0 ? (
        <Empty>Nenhuma caixa {filtro !== 'TODAS' && 'neste status'}.</Empty>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Código</th>
                <th>Nome</th>
                <th>Tamanho</th>
                <th className="num">Espessura</th>
                <th className="num">Chapas</th>
                <th>Entrada</th>
                <th>Status</th>
                <th className="actions" aria-label="Ações" />
              </tr>
            </thead>
            <tbody>
              {visiveis.map((c) => (
                <tr key={c.id}>
                  <td className="mono">{fmt.cx(c.id)}</td>
                  <td>{c.nome}</td>
                  <td className="mono">{c.tamanho}</td>
                  <td className="num mono">{fmt.mm(c.espessura)}</td>
                  <td className="num mono">{fmt.n(c.quantidade)}</td>
                  <td className="mono dim">{fmt.data(c.dataEntrada)}</td>
                  <td>
                    <Status valor={c.status} />
                  </td>
                  <td className="actions">
                    {c.status === 'DISPONIVEL' && (
                      <>
                        <button className="btn-text" onClick={() => abrir(c)}>
                          Editar
                        </button>
                        <ConfirmButton onConfirm={() => excluir(c)} />
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Drawer
        aberto={form !== null}
        titulo={form?.id ? fmt.cx(form.id) : 'Receber caixa'}
        subtitulo={form?.id ? 'Editar caixa' : 'Entrada de material'}
        textoSalvar={form?.id ? 'Salvar' : 'Registrar entrada'}
        salvando={salvando}
        onFechar={() => setForm(null)}
        onSalvar={salvar}
      >
        {form && (
          <>
            <Field
              label="Nome"
              value={form.nome}
              onChange={(e) => setForm({ ...form, nome: e.target.value })}
              erro={campos.nome}
              placeholder="Ex.: Aço DC04 lote 2291"
              maxLength={100}
              required
              autoFocus
            />
            <Field
              label="Tamanho"
              value={form.tamanho}
              onChange={(e) => setForm({ ...form, tamanho: e.target.value })}
              erro={campos.tamanho}
              placeholder="Ex.: 1500 × 3000"
              maxLength={30}
              required
            />
            <div className="field-row">
              <Field
                label="Espessura (mm)"
                inputMode="decimal"
                value={form.espessura}
                onChange={(e) => setForm({ ...form, espessura: e.target.value })}
                erro={campos.espessura}
                placeholder="0,80"
                required
              />
              <Field
                label="Quantidade de chapas"
                type="number"
                min={1}
                value={form.quantidade}
                onChange={(e) => setForm({ ...form, quantidade: e.target.value })}
                erro={campos.quantidade}
                required
              />
            </div>
          </>
        )}
      </Drawer>
    </>
  )
}
