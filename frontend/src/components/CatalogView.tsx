import { useCallback, useEffect, useMemo, useState } from 'react'
import { api, ApiError } from '../api'
import { formatarPreco } from '../format'
import type { Game, GamePrice } from '../types'
import { AddGameModal } from './AddGameModal'
import { Capa } from './Capa'
import { IconeMais, IconeSeta } from './Icons'
import { NewSuggestionModal } from './NewSuggestionModal'
import { useToast } from './Toast'

type PrecoEstado = GamePrice | 'carregando' | 'erro'

export function CatalogView() {
  const toast = useToast()
  const [jogos, setJogos] = useState<Game[]>([])
  const [emVotacao, setEmVotacao] = useState<Set<number>>(new Set())
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [precos, setPrecos] = useState<Record<number, PrecoEstado>>({})
  const [modalAdicionar, setModalAdicionar] = useState(false)
  const [jogoParaSugerir, setJogoParaSugerir] = useState<Game | null>(null)

  const carregar = useCallback(async () => {
    setErro(null)
    try {
      const [catalogo, sugestoes] = await Promise.all([api.listarJogos(), api.listarSugestoes()])
      setJogos(catalogo)
      setEmVotacao(new Set(sugestoes.filter((item) => item.status === 'EM_VOTACAO').map((item) => item.gameId)))
    } catch (falha) {
      setErro(falha instanceof ApiError ? falha.message : 'Não foi possível carregar o catálogo.')
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => {
    void carregar()
  }, [carregar])

  const buscarPreco = useCallback(async (id: number) => {
    setPrecos((atuais) => ({ ...atuais, [id]: 'carregando' }))
    try {
      const preco = await api.precoDoJogo(id)
      setPrecos((atuais) => ({ ...atuais, [id]: preco }))
    } catch {
      setPrecos((atuais) => ({ ...atuais, [id]: 'erro' }))
    }
  }, [])

  const resumo = useMemo(() => {
    const emVotacaoAgora = emVotacao.size
    return `${jogos.length} ${jogos.length === 1 ? 'jogo cadastrado' : 'jogos cadastrados'}${
      emVotacaoAgora > 0 ? ` · ${emVotacaoAgora} em votação` : ''
    }`
  }, [jogos.length, emVotacao.size])

  return (
    <section className="secao">
      <div className="secao__cabecalho">
        <div>
          <h1>Catálogo</h1>
          <p className="muted">Os jogos que a família já cadastrou. Aqui você abre uma votação para qualquer um deles.</p>
        </div>

        <button type="button" className="botao botao--primario" onClick={() => setModalAdicionar(true)}>
          <IconeMais />
          Adicionar jogo
        </button>
      </div>

      {!carregando && !erro && jogos.length > 0 && <p className="muted secao__resumo">{resumo}</p>}

      {erro && (
        <div className="alerta alerta--erro" role="alert">
          <span>{erro}</span>
          <button type="button" className="botao botao--fantasma botao--pequeno" onClick={() => void carregar()}>
            Tentar de novo
          </button>
        </div>
      )}

      {carregando ? (
        <div className="grade grade--jogos" aria-hidden="true">
          {[0, 1, 2, 3].map((indice) => (
            <div key={indice} className="esqueleto esqueleto--jogo" />
          ))}
        </div>
      ) : jogos.length === 0 ? (
        <div className="vazio">
          <span className="vazio__icone" aria-hidden="true">
            <IconeMais size={28} />
          </span>
          <h2>O catálogo está vazio</h2>
          <p className="muted">Busque um jogo pelo nome na loja da Steam e ele aparece aqui.</p>
          <button type="button" className="botao botao--primario" onClick={() => setModalAdicionar(true)}>
            <IconeMais />
            Adicionar o primeiro jogo
          </button>
        </div>
      ) : (
        <div className="grade grade--jogos">
          {jogos.map((jogo) => {
            const preco = precos[jogo.id]
            const ativo = emVotacao.has(jogo.id)

            return (
              <article key={jogo.id} className="jogo">
                <div className="jogo__capa">
                  <Capa src={jogo.imageUrl} alt={`Capa de ${jogo.name}`} nome={jogo.name} />
                  {ativo && <span className="selo selo--em_votacao">Em votação</span>}
                </div>

                <div className="jogo__corpo">
                  <h3 className="jogo__titulo">{jogo.name}</h3>

                  <p className="jogo__preco">
                    {preco === undefined && <span className="muted">App {jogo.steamAppId}</span>}
                    {preco === 'carregando' && <span className="muted">Consultando a Steam…</span>}
                    {preco === 'erro' && <span className="texto-erro">Não foi possível consultar agora</span>}
                    {preco && typeof preco === 'object' && (
                      preco.available ? (
                        <span>
                          <strong>{formatarPreco(preco.finalPrice, preco.currency)}</strong>
                          {preco.discountPercent ? (
                            <span className="desconto"> −{preco.discountPercent}%</span>
                          ) : null}
                          {preco.initial !== null && preco.finalPrice !== null && preco.initial > preco.finalPrice && (
                            <s className="preco-antigo">{formatarPreco(preco.initial, preco.currency)}</s>
                          )}
                        </span>
                      ) : (
                        <span className="muted">Sem preço na região (grátis ou indisponível)</span>
                      )
                    )}
                  </p>

                  <div className="jogo__acoes">
                    <a
                      className="botao botao--fantasma botao--pequeno"
                      href={jogo.storeUrl}
                      target="_blank"
                      rel="noreferrer"
                    >
                      Steam
                      <IconeSeta size={13} />
                    </a>

                    <button
                      type="button"
                      className="botao botao--pequeno"
                      onClick={() => void buscarPreco(jogo.id)}
                      disabled={preco === 'carregando'}
                    >
                      Ver preço
                    </button>

                    <button
                      type="button"
                      className="botao botao--primario botao--pequeno"
                      onClick={() => setJogoParaSugerir(jogo)}
                      disabled={ativo}
                      title={ativo ? 'Este jogo já tem uma sugestão em votação' : 'Abrir uma votação para este jogo'}
                    >
                      {ativo ? 'Já em votação' : 'Sugerir'}
                    </button>
                  </div>
                </div>
              </article>
            )
          })}
        </div>
      )}

      <AddGameModal
        aberto={modalAdicionar}
        jogosExistentes={jogos}
        onFechar={() => setModalAdicionar(false)}
        onAdicionado={(jogo) => {
          setJogos((atuais) => [...atuais, jogo].sort((a, b) => a.name.localeCompare(b.name, 'pt-BR')))
          toast.sucesso(`"${jogo.name}" adicionado ao catálogo.`)
        }}
      />

      <NewSuggestionModal
        aberto={jogoParaSugerir !== null}
        jogos={jogos}
        jogoInicial={jogoParaSugerir}
        onFechar={() => setJogoParaSugerir(null)}
        onCriada={() => {
          setJogoParaSugerir(null)
          void carregar()
        }}
      />
    </section>
  )
}
