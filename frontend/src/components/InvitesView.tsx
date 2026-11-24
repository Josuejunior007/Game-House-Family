import { useCallback, useEffect, useState } from 'react'
import { api, ApiError } from '../api'
import { formatarData } from '../format'
import type { FamilyInvite } from '../types'
import { IconeTrofeu } from './Icons'
import { useToast } from './Toast'

interface Props {
  /** Chamado depois de aceitar: relê o usuário (agora com família) e troca de aba. */
  onAceito: () => Promise<void>
  /** Avisa o App para atualizar o contador do cabeçalho. */
  onMudou: () => void
}

export function InvitesView({ onAceito, onMudou }: Props) {
  const toast = useToast()
  const [convites, setConvites] = useState<FamilyInvite[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [acao, setAcao] = useState<number | null>(null)

  const carregar = useCallback(async () => {
    setErro(null)
    try {
      setConvites(await api.meusConvites())
    } catch (falha) {
      setErro(falha instanceof ApiError ? falha.message : 'Não foi possível carregar os convites.')
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => {
    void carregar()
  }, [carregar])

  async function aceitar(convite: FamilyInvite) {
    setAcao(convite.id)
    try {
      await api.aceitarConvite(convite.id)
      setConvites((atuais) => atuais.filter((item) => item.id !== convite.id))
      toast.sucesso(`Você entrou na família "${convite.familyName}".`)
      onMudou()
      await onAceito()
    } catch (falha) {
      toast.erro(falha instanceof ApiError ? falha.message : 'Não foi possível aceitar o convite.')
    } finally {
      setAcao(null)
    }
  }

  async function recusar(convite: FamilyInvite) {
    setAcao(convite.id)
    try {
      await api.recusarConvite(convite.id)
      setConvites((atuais) => atuais.filter((item) => item.id !== convite.id))
      onMudou()
      toast.info('Convite recusado.')
    } catch (falha) {
      toast.erro(falha instanceof ApiError ? falha.message : 'Não foi possível recusar o convite.')
    } finally {
      setAcao(null)
    }
  }

  return (
    <section className="secao">
      <div className="secao__cabecalho">
        <div>
          <h1>Convites</h1>
          <p className="muted">
            Convites que o dono da família enviou para o seu SteamID. Aceitar entra na família; recusar descarta.
          </p>
        </div>
      </div>

      {erro && (
        <div className="alerta alerta--erro" role="alert">
          <span>{erro}</span>
          <button type="button" className="botao botao--fantasma botao--pequeno" onClick={() => void carregar()}>
            Tentar de novo
          </button>
        </div>
      )}

      {carregando ? (
        <div className="esqueleto" aria-hidden="true" />
      ) : convites.length === 0 ? (
        <div className="vazio">
          <span className="vazio__icone" aria-hidden="true">
            <IconeTrofeu size={28} />
          </span>
          <h2>Nenhum convite pendente</h2>
          <p className="muted">
            Quando o dono adicionar o seu SteamID, o convite aparece aqui. Para ser adicionado, envie o seu ID do
            perfil para ele.
          </p>
        </div>
      ) : (
        <ul className="convites-recebidos">
          {convites.map((convite) => (
            <li className="convite-recebido" key={convite.id}>
              <div className="convite-recebido__info">
                <strong>{convite.familyName}</strong>
                <span className="muted">
                  Convite de {convite.invitedByName} · {formatarData(convite.createdAt)}
                </span>
              </div>
              <div className="convite-recebido__acoes">
                <button
                  type="button"
                  className="botao botao--primario"
                  onClick={() => void aceitar(convite)}
                  disabled={acao === convite.id}
                >
                  {acao === convite.id ? 'Entrando…' : 'Aceitar'}
                </button>
                <button
                  type="button"
                  className="botao botao--fantasma"
                  onClick={() => void recusar(convite)}
                  disabled={acao === convite.id}
                >
                  Recusar
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
