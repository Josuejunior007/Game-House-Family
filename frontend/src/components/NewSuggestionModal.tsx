import { useEffect, useRef, useState, type FormEvent } from 'react'
import { api, ApiError } from '../api'
import type { GameSearchItem, Suggestion } from '../types'
import { useGameSearch } from '../useGameSearch'
import { Capa } from './Capa'
import { IconeBusca, IconeX } from './Icons'
import { Modal } from './Modal'
import { useToast } from './Toast'

interface Props {
  aberto: boolean
  jogoInicial: GameSearchItem | null
  onFechar: () => void
  onCriada: (sugestao: Suggestion) => void
}

const PRAZOS = [1, 3, 7, 14, 30]

export function NewSuggestionModal({ aberto, jogoInicial, onFechar, onCriada }: Props) {
  const toast = useToast()
  const [termo, setTermo] = useState('')
  const [selecionado, setSelecionado] = useState<GameSearchItem | null>(null)
  const [copies, setCopies] = useState(1)
  const [votingDays, setVotingDays] = useState(7)
  const [enviando, setEnviando] = useState(false)
  const [erros, setErros] = useState<Record<string, string>>({})
  const estavaAberto = useRef(false)

  const { resultados, buscando, erro: erroBusca } = useGameSearch(termo)

  // Só reinicia o formulário quando o modal abre — não a cada mudança de props.
  useEffect(() => {
    if (aberto && !estavaAberto.current) {
      setTermo('')
      setSelecionado(jogoInicial)
      setCopies(1)
      setVotingDays(7)
      setErros({})
    }
    estavaAberto.current = aberto
  }, [aberto, jogoInicial])

  async function enviar(evento: FormEvent) {
    evento.preventDefault()

    if (!selecionado) {
      setErros({ jogo: 'Escolha um jogo pela busca.' })
      return
    }
    if (copies < 1) {
      setErros({ copies: 'O número de cópias deve ser maior que 0.' })
      return
    }
    if (votingDays < 1 || votingDays > 30) {
      setErros({ votingDays: 'O prazo deve ser de 1 a 30 dias.' })
      return
    }

    setEnviando(true)
    setErros({})
    try {
      const criada = await api.criarSugestao({ gameId: selecionado.gameId, copies, votingDays })
      toast.sucesso(`Sugestão de "${criada.gameName}" criada.`)
      onCriada(criada)
    } catch (falha) {
      if (falha instanceof ApiError) {
        if (falha.fields) {
          setErros(falha.fields)
        } else {
          setErros({ geral: falha.message })
        }
      } else {
        setErros({ geral: 'Não foi possível criar a sugestão.' })
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Modal aberto={aberto} titulo="Nova sugestão" onFechar={onFechar} largura={560}>
      {!selecionado ? (
        <div className="form">
          <div className="busca">
            <IconeBusca size={18} />
            <input
              value={termo}
              onChange={(evento) => setTermo(evento.target.value)}
              placeholder="Buscar jogo pelo nome (ex.: Elden Ring)"
              aria-label="Buscar jogo"
              autoFocus
            />
            {buscando && <span className="spinner spinner--pequeno" aria-label="Buscando" />}
          </div>

          <p className="muted dica">
            Primeiro o banco (resposta imediata); se o jogo ainda não estiver salvo, a busca vai à Steam e guarda o
            resultado.
          </p>

          {erroBusca && <div className="alerta alerta--erro">{erroBusca}</div>}
          {erros.jogo && <div className="alerta alerta--erro">{erros.jogo}</div>}

          {resultados.length > 0 && (
            <ul className="resultados">
              {resultados.map((item) => (
                <li key={item.steamAppId} className="resultado">
                  <Capa className="resultado__capa" src={item.imageUrl} alt={item.name} nome={item.name} />
                  <div className="resultado__info">
                    <span className="resultado__nome">{item.name}</span>
                    <span className="resultado__preco">
                      {item.source === 'CATALOGO' ? 'Já no catálogo' : 'Salvo agora, vindo da Steam'}
                    </span>
                  </div>
                  <button
                    type="button"
                    className="botao botao--primario botao--pequeno"
                    onClick={() => setSelecionado(item)}
                  >
                    Escolher
                  </button>
                </li>
              ))}
            </ul>
          )}

          {termo.trim().length > 0 && termo.trim().length < 2 && (
            <p className="muted dica">Digite pelo menos 2 letras.</p>
          )}

          {!buscando && termo.trim().length >= 2 && resultados.length === 0 && !erroBusca && (
            <p className="muted dica">Nenhum jogo encontrado com esse nome.</p>
          )}
        </div>
      ) : (
        <form className="form" onSubmit={enviar}>
          <div className="previa">
            <Capa src={selecionado.imageUrl} alt={selecionado.name} nome={selecionado.name} />
            <div>
              <strong>{selecionado.name}</strong>
              <span className="muted">
                {selecionado.source === 'CATALOGO' ? 'Já no catálogo' : 'Salvo no catálogo agora'}
              </span>
            </div>
            <button
              type="button"
              className="botao botao--fantasma botao--pequeno previa__trocar"
              onClick={() => setSelecionado(null)}
            >
              <IconeX size={14} />
              Trocar
            </button>
          </div>

          <label className="campo">
            <span>Quantas cópias?</span>
            <input
              type="number"
              min={1}
              max={99}
              value={copies}
              onChange={(evento) => setCopies(Number(evento.target.value))}
            />
            {erros.copies && <small className="erro-campo">{erros.copies}</small>}
          </label>

          <div className="campo">
            <span>Prazo da votação</span>
            <div className="chips-linha">
              {PRAZOS.map((dias) => (
                <button
                  key={dias}
                  type="button"
                  className={`chip ${votingDays === dias ? 'chip--ativo' : ''}`}
                  onClick={() => setVotingDays(dias)}
                >
                  {dias} {dias === 1 ? 'dia' : 'dias'}
                </button>
              ))}
            </div>
            <input
              type="number"
              min={1}
              max={30}
              value={votingDays}
              onChange={(evento) => setVotingDays(Number(evento.target.value))}
              aria-label="Prazo em dias"
            />
            {erros.votingDays && <small className="erro-campo">{erros.votingDays}</small>}
          </div>

          {erros.geral && <div className="alerta alerta--erro">{erros.geral}</div>}

          <div className="form__acoes">
            <button type="button" className="botao botao--fantasma" onClick={onFechar} disabled={enviando}>
              Cancelar
            </button>
            <button type="submit" className="botao botao--primario" disabled={enviando}>
              {enviando ? 'Criando…' : 'Criar sugestão'}
            </button>
          </div>
        </form>
      )}
    </Modal>
  )
}
