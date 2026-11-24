import { useCallback, useEffect, useMemo, useState } from 'react'
import { api, ApiError } from '../api'
import { formatarPreco } from '../format'
import type { Game, GamePrice, GameSearchItem } from '../types'
import { useGameSearch } from '../useGameSearch'
import { Capa } from './Capa'
import { IconeBusca, IconeSeta } from './Icons'
import { NewSuggestionModal } from './NewSuggestionModal'

type PrecoEstado = GamePrice | 'carregando' | 'erro'

/** Converte um jogo já salvo no mesmo formato da busca, para abrir a sugestão direto do catálogo. */
function itemDoCatalogo(jogo: Game): GameSearchItem {
  return {
    gameId: jogo.id,
    steamAppId: Number(jogo.steamAppId),
    name: jogo.name,
    imageUrl: jogo.imageUrl,
    storeUrl: jogo.storeUrl,
    available: false,
    currency: null,
    initial: null,
    finalPrice: null,
    discountPercent: null,
    source: 'CATALOGO',
  }
}

export function CatalogView() {
  const [jogos, setJogos] = useState<Game[]>([])
  const [emVotacao, setEmVotacao] = useState<Set<number>>(new Set())
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [precos, setPrecos] = useState<Record<number, PrecoEstado>>({})
  const [termo, setTermo] = useState('')
  const [jogoParaSugerir, setJogoParaSugerir] = useState<GameSearchItem | null>(null)

  const { resultados, buscando, erro: erroBusca } = useGameSearch(termo)

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
    return `${jogos.length} ${jogos.length === 1 ? 'jogo no catálogo' : 'jogos no catálogo'}${
      emVotacaoAgora > 0 ? ` · ${emVotacaoAgora} em votação` : ''
    }`
  }, [jogos.length, emVotacao.size])

  const pesquisando = termo.trim().length >= 2

  return (
    <section className="secao">
      <div className="secao__cabecalho">
        <div>
          <h1>Catálogo</h1>
          <p className="muted">
            Busque pelo nome. O que já está no banco responde na hora; o que não estiver vem da Steam e fica salvo
            para a próxima.
          </p>
        </div>
      </div>

      <div className="busca busca--catalogo">
        <IconeBusca size={18} />
        <input
          value={termo}
          onChange={(evento) => setTermo(evento.target.value)}
          placeholder="Buscar jogo pelo nome (ex.: Elden Ring)"
          aria-label="Buscar jogo pelo nome"
        />
        {buscando && <span className="spinner spinner--pequeno" aria-label="Buscando" />}
      </div>

      {erro && (
        <div className="alerta alerta--erro" role="alert">
          <span>{erro}</span>
          <button type="button" className="botao botao--fantasma botao--pequeno" onClick={() => void carregar()}>
            Tentar de novo
          </button>
        </div>
      )}

      {pesquisando ? (
        <>
          {erroBusca && <div className="alerta alerta--erro">{erroBusca}</div>}

          {!buscando && !erroBusca && resultados.length === 0 && (
            <div className="vazio">
              <h2>Nenhum jogo encontrado</h2>
              <p className="muted">Tente outro nome — a busca consulta o catálogo e a loja da Steam.</p>
            </div>
          )}

          {resultados.length > 0 && (
            <ul className="resultados resultados--catalogo">
              {resultados.map((item) => {
                const ativo = emVotacao.has(item.gameId)
                return (
                  <li key={item.steamAppId} className="resultado">
                    <Capa className="resultado__capa" src={item.imageUrl} alt={item.name} nome={item.name} />
                    <div className="resultado__info">
                      <span className="resultado__nome">{item.name}</span>
                      <span className="resultado__preco">
                        {item.source === 'CATALOGO'
                          ? 'Já no catálogo'
                          : item.available
                            ? formatarPreco(item.finalPrice, item.currency)
                            : 'Novo na Steam · sem preço na região'}
                      </span>
                    </div>
                    <div className="resultado__acoes">
                      <a
                        className="botao botao--fantasma botao--pequeno"
                        href={item.storeUrl}
                        target="_blank"
                        rel="noreferrer"
                      >
                        Steam
                        <IconeSeta size={13} />
                      </a>
                      <button
                        type="button"
                        className="botao botao--primario botao--pequeno"
                        onClick={() => setJogoParaSugerir(item)}
                        disabled={ativo}
                        title={ativo ? 'Este jogo já tem uma sugestão em votação' : 'Abrir uma votação para este jogo'}
                      >
                        {ativo ? 'Em votação' : 'Sugerir'}
                      </button>
                    </div>
                  </li>
                )
              })}
            </ul>
          )}
        </>
      ) : carregando ? (
        <div className="grade grade--jogos" aria-hidden="true">
          {[0, 1, 2, 3].map((indice) => (
            <div key={indice} className="esqueleto esqueleto--jogo" />
          ))}
        </div>
      ) : jogos.length === 0 ? (
        <div className="vazio">
          <span className="vazio__icone" aria-hidden="true">
            <IconeBusca size={28} />
          </span>
          <h2>Nada salvo ainda</h2>
          <p className="muted">
            Digite o nome de um jogo acima. Ele é buscado na Steam, salvo no banco e aparece aqui para você sugerir.
          </p>
        </div>
      ) : (
        <>
          <p className="muted secao__resumo">{resumo}</p>
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
                        onClick={() => setJogoParaSugerir(itemDoCatalogo(jogo))}
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
        </>
      )}

      <NewSuggestionModal
        aberto={jogoParaSugerir !== null}
        jogoInicial={jogoParaSugerir}
        onFechar={() => setJogoParaSugerir(null)}
        onCriada={(sugestao) => {
          setJogoParaSugerir(null)
          setEmVotacao((atuais) => new Set(atuais).add(sugestao.gameId))
          void carregar()
        }}
      />
    </section>
  )
}
