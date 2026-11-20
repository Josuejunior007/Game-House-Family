import { useEffect, useRef, useState, type FormEvent } from 'react'
import { api, ApiError } from '../api'
import type { Game, Suggestion } from '../types'
import { Capa } from './Capa'
import { Modal } from './Modal'
import { useToast } from './Toast'

interface Props {
  aberto: boolean
  jogos: Game[]
  jogoInicial: Game | null
  onFechar: () => void
  onCriada: (sugestao: Suggestion) => void
}

const PRAZOS = [1, 3, 7, 14, 30]

export function NewSuggestionModal({ aberto, jogos, jogoInicial, onFechar, onCriada }: Props) {
  const toast = useToast()
  const [gameId, setGameId] = useState('')
  const [copies, setCopies] = useState(1)
  const [votingDays, setVotingDays] = useState(7)
  const [enviando, setEnviando] = useState(false)
  const [erros, setErros] = useState<Record<string, string>>({})
  const estavaAberto = useRef(false)

  // Só reinicia o formulário quando o modal abre — não a cada mudança de props.
  useEffect(() => {
    if (aberto && !estavaAberto.current) {
      setGameId(jogoInicial ? String(jogoInicial.id) : jogos[0] ? String(jogos[0].id) : '')
      setCopies(1)
      setVotingDays(7)
      setErros({})
    }
    estavaAberto.current = aberto
  }, [aberto, jogoInicial, jogos])

  async function enviar(evento: FormEvent) {
    evento.preventDefault()

    const id = Number(gameId)
    if (!id) {
      setErros({ gameId: 'Escolha um jogo.' })
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
      const criada = await api.criarSugestao({ gameId: id, copies, votingDays })
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

  const jogoEscolhido = jogos.find((jogo) => String(jogo.id) === gameId) ?? null

  return (
    <Modal aberto={aberto} titulo="Nova sugestão" onFechar={onFechar} largura={560}>
      <form className="form" onSubmit={enviar}>
        <label className="campo">
          <span>Jogo</span>
          <select value={gameId} onChange={(evento) => setGameId(evento.target.value)}>
            {jogos.length === 0 && <option value="">Nenhum jogo no catálogo</option>}
            {jogos.map((jogo) => (
              <option key={jogo.id} value={jogo.id}>
                {jogo.name}
              </option>
            ))}
          </select>
          {erros.gameId && <small className="erro-campo">{erros.gameId}</small>}
        </label>

        {jogoEscolhido && (
          <div className="previa">
            <Capa src={jogoEscolhido.imageUrl} alt={jogoEscolhido.name} nome={jogoEscolhido.name} />
            <div>
              <strong>{jogoEscolhido.name}</strong>
              <span className="muted">App {jogoEscolhido.steamAppId}</span>
            </div>
          </div>
        )}

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
          <button type="submit" className="botao botao--primario" disabled={enviando || jogos.length === 0}>
            {enviando ? 'Criando…' : 'Criar sugestão'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
