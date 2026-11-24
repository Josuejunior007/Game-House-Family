import { useEffect, useRef, useState } from 'react'
import { api, ApiError } from './api'
import type { GameSearchItem } from './types'

/**
 * Busca de jogos com debounce e descarte de resposta velha. O back-end olha primeiro o banco
 * e só vai à Steam quando falta resultado (o que vier de lá já volta salvo, com gameId).
 */
export function useGameSearch(termo: string) {
  const [resultados, setResultados] = useState<GameSearchItem[]>([])
  const [buscando, setBuscando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const requisicao = useRef(0)

  useEffect(() => {
    const texto = termo.trim()
    const atual = ++requisicao.current

    if (texto.length < 2) {
      setResultados([])
      setBuscando(false)
      setErro(null)
      return
    }

    setBuscando(true)
    const timer = window.setTimeout(async () => {
      try {
        const lista = await api.buscarJogos(texto)
        if (atual !== requisicao.current) return
        setResultados(lista)
        setErro(null)
      } catch (falha) {
        if (atual !== requisicao.current) return
        setResultados([])
        setErro(falha instanceof ApiError ? falha.message : 'Não foi possível buscar os jogos.')
      } finally {
        if (atual === requisicao.current) setBuscando(false)
      }
    }, 400)

    return () => window.clearTimeout(timer)
  }, [termo])

  return { resultados, buscando, erro }
}
