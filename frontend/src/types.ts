export type SuggestionStatus = 'EM_VOTACAO' | 'APROVADA' | 'REJEITADA' | 'COMPRADA'
export type VoteChoice = 'APOIO' | 'NAO_APOIO'

export interface User {
  id: number
  steamId: string
  displayName: string
  avatarUrl: string | null
}

export interface Game {
  id: number
  steamAppId: string
  name: string
  imageUrl: string
  storeUrl: string
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
