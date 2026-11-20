import type { Suggestion } from './types'

/** 4760 -> "R$ 47,60" */
export function formatarPreco(centavos: number | null, moeda: string | null): string {
  if (centavos === null) return '—'
  try {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: moeda ?? 'BRL',
    }).format(centavos / 100)
  } catch {
    return `${(centavos / 100).toFixed(2)} ${moeda ?? ''}`.trim()
  }
}

export function formatarData(iso: string): string {
  return new Date(iso).toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export interface PrazoInfo {
  texto: string
  vencido: boolean
  urgente: boolean
}

/** "faltam 3 dias", "faltam 5 h", "prazo encerrado" — calculado a partir do ISO em UTC. */
export function prazo(iso: string): PrazoInfo {
  const restante = new Date(iso).getTime() - Date.now()
  if (restante <= 0) return { texto: 'Prazo encerrado', vencido: true, urgente: false }

  const minutos = Math.ceil(restante / 60_000)
  if (minutos < 60) return { texto: `faltam ${minutos} min`, vencido: false, urgente: true }

  const horas = Math.ceil(minutos / 60)
  if (horas < 24) return { texto: `faltam ${horas} h`, vencido: false, urgente: true }

  const dias = Math.ceil(horas / 24)
  return { texto: `faltam ${dias} ${dias === 1 ? 'dia' : 'dias'}`, vencido: false, urgente: false }
}

export function iniciais(nome: string): string {
  const partes = nome.trim().split(/\s+/).filter(Boolean)
  if (partes.length === 0) return '?'
  if (partes.length === 1) return partes[0].slice(0, 2).toUpperCase()
  return (partes[0][0] + partes[partes.length - 1][0]).toUpperCase()
}

export const ROTULO_STATUS: Record<Suggestion['status'], string> = {
  EM_VOTACAO: 'Em votação',
  APROVADA: 'Aprovada',
  REJEITADA: 'Rejeitada',
  COMPRADA: 'Comprada',
}

/** A votação só está aberta enquanto o status é EM_VOTACAO e o prazo não venceu. */
export function votacaoAberta(sugestao: Suggestion): boolean {
  return sugestao.status === 'EM_VOTACAO' && new Date(sugestao.votingDeadline).getTime() > Date.now()
}
