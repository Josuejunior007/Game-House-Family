import { useEffect, useRef, useState } from 'react'
import { api, ApiError, extrairAppId } from '../api'
import { formatarPreco } from '../format'
import type { Game, SteamSearchResult } from '../types'
import { IconeBusca, IconeMais } from './Icons'
import { Capa } from './Capa'
import { Modal } from './Modal'
import { useToast } from './Toast'

interface Props {
  aberto: boolean
  jogosExistentes: Game[]
  onFechar: () => void
  onAdicionado: (jogo: Game) => void
}

export function AddGameModal({ aberto, jogosExistentes, onFechar, onAdicionado }: Props) {
  const toast = useToast()
  const [termo, setTermo] = useState('')
  const [resultados, setResultados] = useState<SteamSearchResult[]>([])
  const [buscando, setBuscando] = useState(false)
  const [erroBusca, setErroBusca] = useState<string | null>(null)
  const [adicionando, setAdicionando] = useState<string | null>(null)
  const [manual, setManual] = useState('')
  const [erroManual, setErroManual] = useState<string | null>(null)
  const requisicao = useRef(0)

  useEffect(() => {
    if (!aberto) {
      setTermo('')
      setResultados([])
      setErroBusca(null)
      setManual('')
      setErroManual(null)
      setBuscando(false)
    }
  }, [aberto])

  // Busca com debounce e descarte de resposta velha (se o usuário digitar rápido).
  useEffect(() => {
    const texto = termo.trim()
    if (texto.length < 3) {
      setResultados([])
      setBuscando(false)
      setErroBusca(null)
      return
    }

    setBuscando(true)
    const timer = window.setTimeout(async () => {
      const atual = ++requisicao.current
      try {
        const lista = await api.buscarNaSteam(texto)
        if (atual !== requisicao.current) return
        setResultados(lista)
        setErroBusca(null)
      } catch (falha) {
        if (atual !== requisicao.current) return
        setResultados([])
        setErroBusca(falha instanceof ApiError ? falha.message : 'Falha ao buscar na Steam.')
      } finally {
        if (atual === requisicao.current) setBuscando(false)
      }
    }, 450)

    return () => window.clearTimeout(timer)
  }, [termo])

  const jaNoCatalogo = (appId: number | string) =>
    jogosExistentes.some((jogo) => jogo.steamAppId === String(appId))

  async function adicionar(steamAppId: string) {
    setAdicionando(steamAppId)
    try {
      const jogo = await api.adicionarJogo(steamAppId)
      onAdicionado(jogo)
    } catch (falha) {
      const mensagem = falha instanceof ApiError ? falha.message : 'Não foi possível adicionar o jogo.'
      toast.erro(mensagem)
      setErroManual(mensagem)
    } finally {
      setAdicionando(null)
    }
  }

  function adicionarManual() {
    const appId = extrairAppId(manual)
    if (!appId) {
      setErroManual('Cole o link da loja da Steam ou o appId numérico (só dígitos).')
      return
    }
    setErroManual(null)
    void adicionar(appId)
  }

  return (
    <Modal aberto={aberto} titulo="Adicionar jogo" onFechar={onFechar} largura={640}>
      <div className="busca">
        <IconeBusca size={18} />
        <input
          value={termo}
          onChange={(evento) => setTermo(evento.target.value)}
          placeholder="Buscar na Steam pelo nome (ex.: Elden Ring)"
          autoFocus
        />
        {buscando && <span className="spinner spinner--pequeno" aria-label="Buscando" />}
      </div>

      {termo.trim().length > 0 && termo.trim().length < 3 && (
        <p className="muted dica">Digite pelo menos 3 letras.</p>
      )}

      {erroBusca && <div className="alerta alerta--erro">{erroBusca}</div>}

      {resultados.length > 0 && (
        <ul className="resultados">
          {resultados.map((item) => {
            const jaTem = jaNoCatalogo(item.steamAppId)
            return (
              <li key={item.steamAppId} className="resultado">
                <Capa className="resultado__capa" src={item.imageUrl} alt={item.name} nome={item.name} />
                <div className="resultado__info">
                  <span className="resultado__nome">{item.name}</span>
                  <span className="resultado__preco">
                    {item.available ? formatarPreco(item.finalPrice, item.currency) : 'Sem preço na região'}
                  </span>
                </div>
                <button
                  type="button"
                  className={`botao botao--pequeno ${jaTem ? 'botao--fantasma' : 'botao--primario'}`}
                  disabled={jaTem || adicionando !== null}
                  onClick={() => void adicionar(String(item.steamAppId))}
                >
                  {jaTem ? 'No catálogo' : adicionando === String(item.steamAppId) ? 'Adicionando…' : 'Adicionar'}
                </button>
              </li>
            )
          })}
        </ul>
      )}

      {!buscando && termo.trim().length >= 3 && resultados.length === 0 && !erroBusca && (
        <p className="muted dica">Nenhum jogo encontrado com esse nome.</p>
      )}

      <div className="divisor">
        <span>ou cole o link da loja / appId</span>
      </div>

      <div className="form-linha">
        <input
          className="entrada"
          value={manual}
          onChange={(evento) => setManual(evento.target.value)}
          placeholder="https://store.steampowered.com/app/620/"
          onKeyDown={(evento) => {
            if (evento.key === 'Enter') adicionarManual()
          }}
        />
        <button
          type="button"
          className="botao"
          onClick={adicionarManual}
          disabled={adicionando !== null || manual.trim().length === 0}
        >
          <IconeMais size={16} />
          Adicionar
        </button>
      </div>

      {erroManual && <p className="texto-erro dica">{erroManual}</p>}
    </Modal>
  )
}
