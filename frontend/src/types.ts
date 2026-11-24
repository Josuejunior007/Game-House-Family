export type SuggestionStatus = 'EM_VOTACAO' | 'APROVADA' | 'REJEITADA' | 'COMPRADA'
export type VoteChoice = 'APOIO' | 'NAO_APOIO'
export type FamilyRole = 'DONO' | 'MEMBRO'
export type InviteStatus = 'PENDENTE' | 'ACEITO' | 'RECUSADO' | 'CANCELADO'

export interface User {
  id: number
  steamId: string
  displayName: string
  avatarUrl: string | null
  /** null = ainda não entrou em nenhuma família (o app mostra o onboarding). */
  familyId: number | null
  familyName: string | null
  familyRole: FamilyRole | null
}

export interface Game {
  id: number
  steamAppId: string
  name: string
  imageUrl: string
  storeUrl: string
}

export interface FamilyMember {
  userId: number
  steamId: string
  displayName: string
  avatarUrl: string | null
  role: FamilyRole
  joinedAt: string
}

export interface Family {
  id: number
  name: string
  ownerUserId: number
  ownerName: string
  createdAt: string
  members: FamilyMember[]
}

export interface FamilyInvite {
  id: number
  familyId: number
  familyName: string
  steamId: string
  invitedName: string | null
  invitedAvatarUrl: string | null
  invitedByName: string
  status: InviteStatus
  createdAt: string
}

/** Resultado da busca com cache: sempre tem gameId, então serve direto para criar sugestão. */
export interface GameSearchItem {
  gameId: number
  steamAppId: number
  name: string
  imageUrl: string
  storeUrl: string
  available: boolean
  currency: string | null
  initial: number | null
  finalPrice: number | null
  discountPercent: number | null
  source: 'CATALOGO' | 'STEAM'
}

export interface Suggestion {
  id: number
  userDisplayName: string
  userId: number
  userAvatarUrl: string | null
  gameName: string
  gameId: number
  gameImageUrl: string
  gameStoreUrl: string
  copies: number
  status: SuggestionStatus
  votingDeadline: string
  createdAt: string
  supportVotes: number
  opposeVotes: number
  myVote: VoteChoice | null
}

export interface SteamSearchResult {
  steamAppId: number
  name: string
  imageUrl: string
  storeUrl: string
  available: boolean
  currency: string | null
  initial: number | null
  finalPrice: number | null
  discountPercent: number | null
}

export interface GamePrice {
  available: boolean
  currency: string | null
  initial: number | null
  finalPrice: number | null
  discountPercent: number | null
}

export interface SuggestionCreate {
  gameId: number
  copies: number
  votingDays: number
}
