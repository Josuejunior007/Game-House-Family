import { useState } from 'react'
import { formatarData, prazo, ROTULO_STATUS, votacaoAberta } from '../format'
import type { Suggestion, VoteChoice } from '../types'
import { Avatar } from './Avatar'
import { Capa } from './Capa'
import { IconePolegar, IconePolegarBaixo, IconeRelogio, IconeSacola, IconeSeta, IconeTrofeu } from './Icons'

interface Props {
  sugestao: Suggestion
  usuarioId: number
  ocupado: boolean
  onVotar: (escolha: VoteChoice) => void
  onEncerrar: () => void
  onComprar: () => void
}

export function SuggestionCard({ sugestao, usuarioId, ocupado, onVotar, onEncerrar, onComprar }: Props) {
  const [confirmandoEncerrar, setConfirmandoEncerrar] = useState(false)

  const aberta = votacaoAberta(sugestao)
  const emVotacao = sugestao.status === 'EM_VOTACAO'
  const info = prazo(sugestao.votingDeadline)
  const total = sugestao.supportVotes + sugestao.opposeVotes
  const pctApoio = total === 0 ? 0 : Math.round((sugestao.supportVotes / total) * 100)
  const ehCriador = sugestao.userId === usuarioId
  // RN-15: o criador encerra quando quiser; os demais só depois do prazo.
  const podeEncerrar = emVotacao && (ehCriador || info.vencido)

  return (
    <article className={`cartao cartao--${sugestao.status.toLowerCase()}`}>
      <div className="cartao__capa">
        <Capa src={sugestao.gameImageUrl} alt={`Capa de ${sugestao.gameName}`} nome={sugestao.gameName} />
        <span className={`selo selo--${sugestao.status.toLowerCase()}`}>{ROTULO_STATUS[sugestao.status]}</span>
        <a
          className="cartao__link-loja"
          href={sugestao.gameStoreUrl}
          target="_blank"
          rel="noreferrer"
          aria-label={`Abrir ${sugestao.gameName} na Steam`}
          title="Abrir na Steam"
        >
          <IconeSeta size={16} />
        </a>
      </div>

      <div className="cartao__corpo">
        <h3 className="cartao__titulo">{sugestao.gameName}</h3>

        <p className="cartao__meta">
          <Avatar nome={sugestao.userDisplayName} url={sugestao.userAvatarUrl} tamanho={22} />
          <span>
            <strong>{ehCriador ? 'Você' : sugestao.userDisplayName}</strong> sugeriu
          </span>
          <span className="ponto">·</span>
          <span>
            {sugestao.copies} {sugestao.copies === 1 ? 'cópia' : 'cópias'}
          </span>
        </p>

        <div className="votos">
          <button
            type="button"
            className={`voto voto--apoio ${sugestao.myVote === 'APOIO' ? 'voto--ativo' : ''}`}
            onClick={() => onVotar('APOIO')}
            disabled={!aberta || ocupado}
            aria-pressed={sugestao.myVote === 'APOIO'}
            title={aberta ? 'Votar a favor' : 'A votação não está aberta'}
          >
            <IconePolegar size={16} />
            <span>Apoio</span>
            <strong>{sugestao.supportVotes}</strong>
          </button>

          <button
            type="button"
            className={`voto voto--nao ${sugestao.myVote === 'NAO_APOIO' ? 'voto--ativo' : ''}`}
            onClick={() => onVotar('NAO_APOIO')}
            disabled={!aberta || ocupado}
            aria-pressed={sugestao.myVote === 'NAO_APOIO'}
            title={aberta ? 'Votar contra' : 'A votação não está aberta'}
          >
            <IconePolegarBaixo size={16} />
            <span>Não apoio</span>
            <strong>{sugestao.opposeVotes}</strong>
          </button>
        </div>

        {total > 0 ? (
          <div className="barra" aria-hidden="true">
            <span className="barra__apoio" style={{ width: `${pctApoio}%` }} />
            <span className="barra__nao" style={{ width: `${100 - pctApoio}%` }} />
          </div>
        ) : (
          <p className="cartao__sem-votos">Ninguém votou ainda — seja o primeiro.</p>
        )}

        <div className="cartao__rodape">
          <span
            className={`prazo ${info.vencido ? 'prazo--vencido' : ''} ${info.urgente ? 'prazo--urgente' : ''}`}
            title={`Prazo: ${formatarData(sugestao.votingDeadline)}`}
          >
            <IconeRelogio size={14} />
            {emVotacao ? info.texto : formatarData(sugestao.votingDeadline)}
          </span>

          <div className="cartao__acoes">
            {sugestao.status === 'APROVADA' && (
              <button type="button" className="botao botao--primario botao--pequeno" onClick={onComprar} disabled={ocupado}>
                <IconeSacola size={15} />
                Já comprei
              </button>
            )}

            {sugestao.status === 'COMPRADA' && (
              <span className="etiqueta etiqueta--sucesso">
                <IconeTrofeu size={14} />
                Comprada
              </span>
            )}

            {podeEncerrar && !confirmandoEncerrar && (
              <button
                type="button"
                className="botao botao--pequeno"
                onClick={() => setConfirmandoEncerrar(true)}
                disabled={ocupado}
              >
                Encerrar votação
              </button>
            )}

            {podeEncerrar && confirmandoEncerrar && (
              <span className="confirmacao">
                <span>Tem certeza?</span>
                <button
                  type="button"
                  className="botao botao--perigo botao--pequeno"
                  onClick={() => {
                    setConfirmandoEncerrar(false)
                    onEncerrar()
                  }}
                  disabled={ocupado}
                >
                  Encerrar
                </button>
                <button
                  type="button"
                  className="botao botao--fantasma botao--pequeno"
                  onClick={() => setConfirmandoEncerrar(false)}
                >
                  Cancelar
                </button>
              </span>
            )}
          </div>
        </div>
      </div>
    </article>
  )
}
