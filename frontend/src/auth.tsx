import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api, ApiError } from './api'
import type { User } from './types'

interface AuthContextValue {
  usuario: User | null
  carregando: boolean
  entrarComSteam: () => void
  entrarDev: (steamId: string, nome: string) => Promise<void>
  sair: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<User | null>(null)
  const [carregando, setCarregando] = useState(true)

  // Ao abrir a página, pergunta quem está logado. 401 é resposta normal (ninguém logado).
  useEffect(() => {
    let ativo = true

    api
      .me()
      .then((dados) => {
        if (ativo) setUsuario(dados)
      })
      .catch((erro: unknown) => {
        if (erro instanceof ApiError && erro.status === 401) return
        console.error('Falha ao checar a sessão', erro)
      })
      .finally(() => {
        if (ativo) setCarregando(false)
      })

    return () => {
      ativo = false
    }
  }, [])

  const entrarComSteam = useCallback(() => {
    window.location.assign(api.urlLoginSteam())
  }, [])

  const entrarDev = useCallback(async (steamId: string, nome: string) => {
    const dados = await api.devLogin(steamId, nome)
    setUsuario(dados)
  }, [])

  const sair = useCallback(async () => {
    await api.logout()
    setUsuario(null)
  }, [])

  const valor = useMemo<AuthContextValue>(
    () => ({ usuario, carregando, entrarComSteam, entrarDev, sair }),
    [usuario, carregando, entrarComSteam, entrarDev, sair],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const contexto = useContext(AuthContext)
  if (!contexto) throw new Error('useAuth precisa estar dentro de <AuthProvider>')
  return contexto
}
