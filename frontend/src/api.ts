import type {
  Game,
  GamePrice,
  SteamSearchResult,
  Suggestion,
  SuggestionCreate,
  SuggestionStatus,
  User,
  VoteChoice,
} from './types'

/** URL da API. Em dev, o back-end roda em 8081. Em produção, use a mesma origem (VITE_API_URL=''). */
const BASE = (import.meta.env.VITE_API_URL ?? 'http://localhost:8081').replace(/\/+$/, '')

/** Erro da API já no formato único do back-end: { status, message, fields }. */
export class ApiError extends Error {
  readonly status: number
  readonly fields: Record<string, string> | null

  constructor(message: string, status: number, fields: Record<string, string> | null = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fields = fields
  }
}

interface CorpoDeErro {
  status?: number
  message?: string
  fields?: Record<string, string> | null
}

async function request<T>(caminho: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  if (init.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  let resposta: Response
  try {
    resposta = await fetch(`${BASE}${caminho}`, { ...init, headers, credentials: 'include' })
  } catch {
    throw new ApiError('Não foi possível falar com a API. Ela está rodando em ' + BASE + '?', 0)
  }

  if (resposta.status === 204) {
    return undefined as T
  }

  const texto = await resposta.text()
  let dados: unknown = null
  if (texto) {
    try {
      dados = JSON.parse(texto)
    } catch {
      dados = texto
    }
  }

  if (!resposta.ok) {
    const corpo = (dados ?? {}) as CorpoDeErro
    const mensagem =
      typeof corpo.message === 'string' && corpo.message ? corpo.message : `Erro ${resposta.status} ao chamar a API.`
    throw new ApiError(mensagem, resposta.status, corpo.fields ?? null)
  }

  return dados as T
}

const json = (corpo: unknown) => JSON.stringify(corpo)

export const api = {
  /** URL para abrir o login real da Steam no navegador (redireciona de volta para o front). */
  urlLoginSteam: () => `${BASE}/auth/steam/login`,

  me: () => request<User>('/auth/me'),

  devLogin: (steamId: string, displayName: string) =>
    request<User>('/auth/dev-login', { method: 'POST', body: json({ steamId, displayName }) }),

  logout: () => request<void>('/auth/logout', { method: 'POST' }),

  listarJogos: () => request<Game[]>('/games'),

  buscarJogo: (id: number) => request<Game>(`/games/${id}`),

  adicionarJogo: (steamAppId: string) =>
    request<Game>('/games', { method: 'POST', body: json({ steamAppId }) }),

  precoDoJogo: (id: number) => request<GamePrice>(`/games/${id}/price`),

  buscarNaSteam: (termo: string) =>
    request<SteamSearchResult[]>(`/steam/search?q=${encodeURIComponent(termo)}`),

  listarSugestoes: (status?: SuggestionStatus) =>
    request<Suggestion[]>(`/suggestions${status ? `?status=${status}` : ''}`),

  criarSugestao: (dados: SuggestionCreate) =>
    request<Suggestion>('/suggestions', { method: 'POST', body: json(dados) }),

  votar: (id: number, choice: VoteChoice) =>
    request<Suggestion>(`/suggestions/${id}/vote`, { method: 'PUT', body: json({ choice }) }),

  encerrar: (id: number) => request<Suggestion>(`/suggestions/${id}/close`, { method: 'POST' }),

  marcarComoComprada: (id: number) =>
    request<Suggestion>(`/suggestions/${id}/purchase`, { method: 'POST' }),
}

/** Aceita "620", "https://store.steampowered.com/app/620/Portal_2/" ou o appId colado com espaços. */
export function extrairAppId(entrada: string): string | null {
  const texto = entrada.trim()
  if (!texto) return null
  const porUrl = texto.match(/\/app\/(\d+)/)
  if (porUrl) return porUrl[1]
  if (/^\d+$/.test(texto)) return texto
  return null
}
