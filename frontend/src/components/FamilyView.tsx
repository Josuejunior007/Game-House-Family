import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { api, ApiError } from '../api'
import { useAuth } from '../auth'
import { formatarData } from '../format'
import type { Family, FamilyInvite } from '../types'
import { Avatar } from './Avatar'
import { IconeMais, IconeTrofeu } from './Icons'
import { useToast } from './Toast'

interface Props {
  onAtualizar: () => Promise<void>
  onAtualizarConvites: () => Promise<void>
}

/** Fallback para navegador sem Clipboard API (ou que nega a permissão). */
function copiarComSelecao(texto: string) {
  const area = document.createElement('textarea')
  area.value = texto
  area.setAttribute('readonly', '')
  area.style.position = 'fixed'
  area.style.top = '-1000px'
  area.style.opacity = '0'
  document.body.appendChild(area)
  area.select()
  const copiou = document.execCommand('copy')
  document.body.removeChild(area)
  if (!copiou) throw new Error('execCommand("copy") falhou')
}

export function FamilyView({ onAtualizar, onAtualizarConvites }: Props) {
  const { usuario } = useAuth()
  const toast = useToast()

  const [familia, setFamilia] = useState<Family | null>(null)
  const [convites, setConvites] = useState<FamilyInvite[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)

  const [nome, setNome] = useState('')
  const [steamId, setSteamId] = useState('')
  const [criando, setCriando] = useState(false)
  const [convidando, setConvidando] = useState(false)
  const [acao, setAcao] = useState<string | null>(null)
  const [confirmacao, setConfirmacao] = useState<string | null>(null)

  const souDono = usuario?.familyRole === 'DONO' || (familia != null && familia.ownerUserId === usuario?.id)

  const carregar = useCallback(async () => {
    setErro(null)
    try {
      const f = await api.minhaFamilia()
      setFamilia(f)
      setConvites(f && usuario?.familyRole === 'DONO' ? await api.convitesEnviados() : [])
    } catch (falha) {
      setErro(falha instanceof ApiError ? falha.message : 'Não foi possível carregar a família.')
    } finally {
      setCarregando(false)
    }
  }, [usuario?.familyRole])

  useEffect(() => {
    void carregar()
  }, [carregar])

  if (!usuario) return null

  async function copiarSteamId() {
    const texto = usuario!.steamId
    try {
      if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(texto)
      } else {
        copiarComSelecao(texto)
      }
      toast.sucesso('SteamID64 copiado.')
    } catch {
      // clipboard moderno pode ser negado (http, permissão, navegador antigo): tenta o método clássico
      try {
        copiarComSelecao(texto)
        toast.sucesso('SteamID64 copiado.')
      } catch {
        toast.erro('O navegador não deixou copiar. Selecione o número e copie manualmente.')
      }
    }
  }

  async function criarFamilia(evento: FormEvent) {
    evento.preventDefault()
    setCriando(true)
    try {
      const familiaCriada = await api.criarFamilia(nome.trim())
      setFamilia(familiaCriada)
      setNome('')
      await onAtualizar()
      toast.sucesso(`Família "${familiaCriada.name}" criada. Agora adicione as pessoas pelo SteamID.`)
    } catch (falha) {
      toast.erro(falha instanceof ApiError ? falha.message : 'Não foi possível criar a família.')
    } finally {
      setCriando(false)
    }
  }

  async function convidar(evento: FormEvent) {
    evento.preventDefault()
    const id = steamId.trim()
    if (!/^\d{17}$/.test(id)) {
      toast.erro('O SteamID64 tem 17 números (começa com 7656119…).')
      return
    }
    setConvidando(true)
    try {
      const convite = await api.convidar(id)
      setConvites((atuais) => [convite, ...atuais])
      setSteamId('')
      toast.sucesso('Convite criado. A pessoa aceita na aba Convites quando entrar com a Steam.')
    } catch (falha) {
      toast.erro(falha instanceof ApiError ? falha.message : 'Não foi possível convidar.')
    } finally {
      setConvidando(false)
    }
  }

  async function cancelarConvite(id: number) {
    setAcao(`convite-${id}`)
    try {
      await api.cancelarConvite(id)
      setConvites((atuais) => atuais.filter((convite) => convite.id !== id))
      toast.sucesso('Convite cancelado.')
    } catch (falha) {
      toast.erro(falha instanceof ApiError ? falha.message : 'Não foi possível cancelar o convite.')
    } finally {
      setAcao(null)
    }
  }

  async function removerMembro(userId: number, nomeMembro: string) {
    setAcao(`membro-${userId}`)
    try {
      await api.removerMembro(userId)
      setFamilia((atual) =>
        atual ? { ...atual, members: atual.members.filter((membro) => membro.userId !== userId) } : atual,
      )
      setConfirmacao(null)
      toast.sucesso(`${nomeMembro} saiu da família.`)
    } catch (falha) {
      toast.erro(falha instanceof ApiError ? falha.message : 'Não foi possível remover.')
    } finally {
      setAcao(null)
    }
  }

  async function sairDaFamilia() {
    setAcao('sair')
    try {
      await api.sairDaFamilia()
      setFamilia(null)
      setConfirmacao(null)
      await onAtualizar()
      await onAtualizarConvites()
      toast.sucesso('Você saiu da família.')
    } catch (falha) {
      toast.erro(falha instanceof ApiError ? falha.message : 'Não foi possível sair.')
    } finally {
      setAcao(null)
    }
  }

  async function excluirFamilia() {
    setAcao('excluir')
    try {
      await api.excluirFamilia()
      setFamilia(null)
      setConvites([])
      setConfirmacao(null)
      await onAtualizar()
      toast.sucesso('Família excluída.')
    } catch (falha) {
      toast.erro(falha instanceof ApiError ? falha.message : 'Não foi possível excluir a família.')
    } finally {
      setAcao(null)
    }
  }

  return (
    <section className="secao">
      <div className="secao__cabecalho">
        <div>
          <h1>Família</h1>
          <p className="muted">
            Seu perfil, seu SteamID64 e quem está na família. Só o dono adiciona ou remove pessoas.
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
        <div className="esqueleto esqueleto--perfil" aria-hidden="true" />
      ) : (
        <>
          <article className="perfil">
            <Avatar nome={usuario.displayName} url={usuario.avatarUrl} tamanho={66} />
            <div className="perfil__dados">
              <div className="perfil__linha">
                <h2>{usuario.displayName}</h2>
                {usuario.familyRole && (
                  <span className={`etiqueta ${souDono ? 'etiqueta--dono' : 'etiqueta--membro'}`}>
                    {souDono ? 'Dono da família' : 'Membro'}
                  </span>
                )}
              </div>
              <span className="perfil__rotulo">SteamID64</span>
              <div className="perfil__id">
                <code>{usuario.steamId}</code>
                <button type="button" className="botao botao--pequeno" onClick={() => void copiarSteamId()}>
                  Copiar
                </button>
                <a
                  className="botao botao--fantasma botao--pequeno"
                  href={`https://steamcommunity.com/profiles/${usuario.steamId}`}
                  target="_blank"
                  rel="noreferrer"
                >
                  Abrir na Steam
                </a>
              </div>
            </div>
          </article>

          {!familia ? (
            <div className="onboarding">
              <span className="vazio__icone" aria-hidden="true">
                <IconeTrofeu size={28} />
              </span>
              <h2>Você ainda não está em uma família</h2>
              <p className="muted">
                Se você é o dono, crie a família agora. Se alguém já te adicionou pelo SteamID, o convite aparece na
                aba <strong>Convites</strong>.
              </p>

              <form className="form-linha onboarding__form" onSubmit={criarFamilia}>
                <input
                  className="entrada"
                  value={nome}
                  onChange={(evento) => setNome(evento.target.value)}
                  placeholder="Nome da família (ex.: Família do Josué)"
                  maxLength={60}
                  required
                />
                <button
                  type="submit"
                  className="botao botao--primario"
                  disabled={criando || nome.trim().length < 2}
                >
                  <IconeMais size={16} />
                  {criando ? 'Criando…' : 'Criar família'}
                </button>
              </form>
              <small className="muted">
                Depois de criar, copie o seu SteamID e adicione as pessoas. Cada pessoa entra em uma família por vez.
              </small>
            </div>
          ) : (
            <div className="familia">
              <div className="familia__topo">
                <div>
                  <h2>{familia.name}</h2>
                  <p className="muted">
                    Dono: <strong>{familia.ownerName}</strong> · criada em {formatarData(familia.createdAt)} ·{' '}
                    {familia.members.length} {familia.members.length === 1 ? 'membro' : 'membros'}
                  </p>
                </div>
              </div>

              <ul className="membros">
                {familia.members.map((membro) => (
                  <li className="membro" key={membro.userId}>
                    <Avatar nome={membro.displayName} url={membro.avatarUrl} tamanho={40} />
                    <div className="membro__info">
                      <span className="membro__nome">
                        {membro.displayName}
                        {membro.userId === usuario.id ? ' (você)' : ''}
                      </span>
                      <span className="membro__id">{membro.steamId}</span>
                    </div>
                    <span className={`etiqueta ${membro.role === 'DONO' ? 'etiqueta--dono' : 'etiqueta--membro'}`}>
                      {membro.role === 'DONO' ? 'Dono' : 'Membro'}
                    </span>

                    {souDono && membro.userId !== usuario.id && (
                      <span className="confirmacao">
                        {confirmacao === `membro-${membro.userId}` ? (
                          <>
                            <span>Remover?</span>
                            <button
                              type="button"
                              className="botao botao--perigo botao--pequeno"
                              onClick={() => void removerMembro(membro.userId, membro.displayName)}
                              disabled={acao === `membro-${membro.userId}`}
                            >
                              Sim
                            </button>
                            <button
                              type="button"
                              className="botao botao--fantasma botao--pequeno"
                              onClick={() => setConfirmacao(null)}
                            >
                              Não
                            </button>
                          </>
                        ) : (
                          <button
                            type="button"
                            className="botao botao--fantasma botao--pequeno"
                            onClick={() => setConfirmacao(`membro-${membro.userId}`)}
                          >
                            Remover
                          </button>
                        )}
                      </span>
                    )}
                  </li>
                ))}
              </ul>

              {souDono && (
                <div className="painel">
                  <h3>Adicionar pessoa pelo SteamID</h3>
                  <p className="muted painel__dica">
                    Peça o SteamID64 (17 números). A pessoa entra com a Steam e aceita o convite na aba Convites.
                  </p>
                  <form className="form-linha" onSubmit={convidar}>
                    <input
                      className="entrada"
                      value={steamId}
                      onChange={(evento) => setSteamId(evento.target.value.replace(/\D/g, '').slice(0, 17))}
                      inputMode="numeric"
                      placeholder="7656119…"
                      aria-label="SteamID64 de quem você quer adicionar"
                    />
                    <button
                      type="submit"
                      className="botao botao--primario"
                      disabled={convidando || steamId.length !== 17}
                    >
                      <IconeMais size={16} />
                      {convidando ? 'Convidando…' : 'Convidar'}
                    </button>
                  </form>

                  {convites.length > 0 && (
                    <>
                      <h4 className="painel__subtitulo">Convites pendentes</h4>
                      <ul className="convites-lista">
                        {convites.map((convite) => (
                          <li className="convite-linha" key={convite.id}>
                            <div>
                              <span className="convite-linha__nome">
                                {convite.invitedName ?? `SteamID ${convite.steamId}`}
                              </span>
                              <span className="convite-linha__data">
                                Enviado em {formatarData(convite.createdAt)}
                              </span>
                            </div>
                            <button
                              type="button"
                              className="botao botao--fantasma botao--pequeno"
                              onClick={() => void cancelarConvite(convite.id)}
                              disabled={acao === `convite-${convite.id}`}
                            >
                              Cancelar
                            </button>
                          </li>
                        ))}
                      </ul>
                    </>
                  )}
                </div>
              )}

              <div className={`painel ${souDono ? 'painel--perigo' : ''}`}>
                {souDono ? (
                  <>
                    <h3>Excluir a família</h3>
                    <p className="muted painel__dica">
                      Remove todos os membros e convites. As sugestões e o catálogo continuam no banco.
                    </p>
                    {confirmacao === 'excluir' ? (
                      <div className="confirmacao">
                        <span>Tem certeza? Não dá para desfazer.</span>
                        <button
                          type="button"
                          className="botao botao--perigo"
                          onClick={() => void excluirFamilia()}
                          disabled={acao === 'excluir'}
                        >
                          Excluir família
                        </button>
                        <button
                          type="button"
                          className="botao botao--fantasma"
                          onClick={() => setConfirmacao(null)}
                        >
                          Cancelar
                        </button>
                      </div>
                    ) : (
                      <button
                        type="button"
                        className="botao botao--perigo"
                        onClick={() => setConfirmacao('excluir')}
                      >
                        Excluir família
                      </button>
                    )}
                  </>
                ) : (
                  <>
                    <h3>Sair da família</h3>
                    <p className="muted painel__dica">Você pode sair quando quiser; o dono pode te adicionar de novo.</p>
                    {confirmacao === 'sair' ? (
                      <div className="confirmacao">
                        <span>Tem certeza?</span>
                        <button
                          type="button"
                          className="botao botao--perigo"
                          onClick={() => void sairDaFamilia()}
                          disabled={acao === 'sair'}
                        >
                          Sair
                        </button>
                        <button
                          type="button"
                          className="botao botao--fantasma"
                          onClick={() => setConfirmacao(null)}
                        >
                          Cancelar
                        </button>
                      </div>
                    ) : (
                      <button type="button" className="botao" onClick={() => setConfirmacao('sair')}>
                        Sair da família
                      </button>
                    )}
                  </>
                )}
              </div>
            </div>
          )}
        </>
      )}
    </section>
  )
}
