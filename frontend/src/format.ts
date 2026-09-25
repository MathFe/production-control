const numero = new Intl.NumberFormat('pt-BR')
const decimal = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const dataCurta = new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: '2-digit', year: '2-digit' })
const hora = new Intl.DateTimeFormat('pt-BR', { hour: '2-digit', minute: '2-digit' })
const hoje = new Intl.DateTimeFormat('pt-BR', { weekday: 'short', day: '2-digit', month: 'short' })

export const fmt = {
  n: (v: number) => numero.format(v),
  mm: (v: number) => `${decimal.format(v)} mm`,
  data: (iso: string) => dataCurta.format(new Date(iso)),
  dataHora: (iso: string) => `${dataCurta.format(new Date(iso))} ${hora.format(new Date(iso))}`,
  hoje: () => hoje.format(new Date()).replace(/\./g, ''),
  op: (id: number) => `OP-${String(id).padStart(4, '0')}`,
  cx: (id: number) => `CX-${String(id).padStart(3, '0')}`,
}
