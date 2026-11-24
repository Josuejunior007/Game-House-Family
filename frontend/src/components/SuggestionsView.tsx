import { useCallback, useEffect, useMemo, useState } from 'react'
import { api, ApiError } from '../api'
import type { Suggestion, SuggestionStatus, User, VoteChoice } from '../types'
import { IconeMais, IconeTrofeu } from './Icons'
import { NewSuggestionModal } from './NewSuggestionModal'
import { SuggestionCard } from './SuggestionCard'
import { useToast } from './Toast'

type Filtro = 'TODAS' | SuggestionStatus

const FILTROS: { valor: Filtro; rotulo: string }[] = [
  { valor: 'TODAS', rotulo: 'Todas' },
  { valor: 'EM_VOTACAO', rotulo: 'Em votação' },
  { valor: 'APROVADA', rotulo: 'Aprovadas' },
  { valor: 'COMPRADA', rotulo: 'Compradas' },
  { valor: 'REJEITADA', rotulo: 'Rejeitadas' },
]

export function SuggestionsView({ usuario }: { usuario: User }) {
  const toast = useToast()
  const [sugestoes, setSugestoes] = useState<Suggestion[]>([])
  const [filtro, setFiltro] = useState<Filtro>('TODAS')
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [emAcao, setEmAcao] = useState<number | null>(null)
  const [modalNova, setModalNova] = useState(false)

  const carregar = useCallback(async () => {
    setErro(null)
    try {
      setSugestoes(await api.listarSugestoes())
    } catch (falha) {
      setErro(falha instanceof ApiError ? falha.message : 'Não foi possível carregar as sugestões.')
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => {
    void carregar()
  }, [carregar])

  const executar = useCallback(
    async (id: number, acao: () => Promise<Suggestion>, mensagem: string) => {
      setEmAcao(id)
      try {
        const atualizada = await acao()
        setSugestoes((atuais) => atuais.map((item) => (item.id === atualizada.id ? atualizada : item)))
        toast.sucesso(mensagem)
      } catch (falha) {
        toast.erro(falha instanceof ApiError ? falha.message : 'Não deu certo. Tente novamente.')
      } finally {
        setEmAcao(null)
      }
    },
    [toast],
  )

  const excluir = useCallback(
    async (id: number) => {
      setEmAcao(id)
      try {
        await api.excluirSugestao(id)
        setSugestoes((atuais) => atuais.filter((item) => item.id !== id))
        toast.sucesso('Sugestão excluída.')
      } catch (falha) {
        toast.erro(falha instanceof ApiError ? falha.message : 'Não foi possível excluir a sugestão.')
      } finally {
        setEmAcao(null)
      }
    },
    [toast],
  )

  const contar = useMemo(() => {
    const mapa = new Map<Filtro, number>()
    mapa.set('TODAS', sugestoes.length)
    for (const sugestao of sugestoes) {
      mapa.set(sugestao.status, (mapa.get(sugestao.status) ?? 0) + 1)
    }
    return mapa
  }, [sugestoes])

  const visiveis = useMemo(
    () => (filtro === 'TODAS' ? sugestoes : sugestoes.filter((item) => item.status === filtro)),
    [sugestoes, filtro],
  )

  return (
    <section className="secao">
      <div className="secao__cabecalho">
        <div>
          <h1>Sugestões</h1>
          <p className="muted">
            Cada sugestão tem um prazo. Quando ele acaba — ou o criador encerra antes — vale o placar: mais apoio do
            que recusa aprova o jogo.
          </p>
        </div>

        <button type="button" className="botao botao--primario" onClick={() => setModalNova(true)}>
          <IconeMais />
          Nova sugestão
        </button>
      </div>

      <div className="filtros" role="tablist" aria-label="Filtrar por status">
        {FILTROS.map((item) => {
          const quantidade = contar.get(item.valor) ?? 0
          const ativo = filtro === item.valor
          return (
            <button
              key={item.valor}
              type="button"
              role="tab"
              aria-selected={ativo}
              className={`chip ${ativo ? 'chip--ativo' : ''}`}
              onClick={() => setFiltro(item.valor)}
            >
              {item.rotulo}
              <span className="chip__contador">{quantidade}</span>
            </button>
          )
        })}
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
        <div className="grade" aria-hidden="true">
          {[0, 1, 2].map((indice) => (
            <div key={indice} className="esqueleto" />
          ))}
        </div>
      ) : visiveis.length === 0 ? (
        <div className="vazio">
          <span className="vazio__icone" aria-hidden="true">
            <IconeTrofeu size={28} />
          </span>
          <h2>{filtro === 'TODAS' ? 'Nenhuma sugestão ainda' : 'Nada com esse status'}</h2>
          <p className="muted">
            {filtro === 'TODAS'
              ? 'Busque um jogo pelo nome e abra a primeira votação da família.'
              : 'Tente outro filtro para ver as outras sugestões.'}
          </p>
          {filtro === 'TODAS' && (
            <button type="button" className="botao botao--primario" onClick={() => setModalNova(true)}>
              <IconeMais />
              Criar sugestão
            </button>
          )}
        </div>
      ) : (
        <div className="grade">
          {visiveis.map((sugestao) => (
            <SuggestionCard
              key={sugestao.id}
              sugestao={sugestao}
              usuarioId={usuario.id}
              podeExcluir={sugestao.userId === usuario.id || usuario.familyRole === 'DONO'}
              ocupado={emAcao === sugestao.id}
              onVotar={(escolha: VoteChoice) =>
                void executar(
                  sugestao.id,
                  () => api.votar(sugestao.id, escolha),
                  escolha === 'APOIO' ? 'Voto de apoio registrado.' : 'Voto contrário registrado.',
                )
              }
              onEncerrar={() =>
                void executar(sugestao.id, () => api.encerrar(sugestao.id), 'Votação encerrada.')
              }
              onComprar={() =>
                void executar(
                  sugestao.id,
                  () => api.marcarComoComprada(sugestao.id),
                  'Sugestão marcada como comprada.',
                )
              }
              onExcluir={() => void excluir(sugestao.id)}
            />
          ))}
        </div>
      )}

      <NewSuggestionModal
        aberto={modalNova}
        jogoInicial={null}
        onFechar={() => setModalNova(false)}
        onCriada={() => {
          setModalNova(false)
          void carregar()
        }}
      />
    </section>
  )
}
