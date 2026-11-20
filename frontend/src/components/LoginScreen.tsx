import { useState, type FormEvent } from 'react'
import { ApiError } from '../api'
import { useAuth } from '../auth'
import { IconeSteam } from './Icons'
import { useToast } from './Toast'

const CONTAS_DEMO = [
  { steamId: '76561198000000001', nome: 'Ana' },
  { steamId: '76561198000000002', nome: 'Beto' },
]

export function LoginScreen() {
  const { entrarComSteam, entrarDev } = useAuth()
  const toast = useToast()
  const [steamId, setSteamId] = useState(CONTAS_DEMO[0].steamId)
  const [nome, setNome] = useState(CONTAS_DEMO[0].nome)
  const [enviando, setEnviando] = useState(false)

  async function entrar(evento: FormEvent) {
    evento.preventDefault()
    setEnviando(true)
    try {
      await entrarDev(steamId.trim(), nome.trim())
    } catch (erro) {
      toast.erro(erro instanceof ApiError ? erro.message : 'Não foi possível entrar.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <main className="login">
      <section className="login__cartao">
        <span className="login__marca" aria-hidden="true">
          <IconeSteam size={30} />
        </span>

        <h1>Game House Family</h1>
        <p className="login__subtitulo">
          Alguém sugere um jogo, a família vota e, se a maioria apoiar, fica combinado quem compra. Simples assim.
        </p>

        <button type="button" className="botao botao--steam botao--largo" onClick={entrarComSteam}>
          <IconeSteam size={20} />
          Entrar com a Steam
        </button>

        {import.meta.env.DEV && (
          <>
            <div className="login__divisor">
              <span>ou entre com uma conta de teste</span>
            </div>

            <form className="login__form" onSubmit={entrar}>
              <div className="campo-grupo">
                <label className="campo">
                  <span>SteamID64</span>
                  <input
                    value={steamId}
                    onChange={(evento) => setSteamId(evento.target.value)}
                    inputMode="numeric"
                    autoComplete="off"
                    required
                  />
                </label>
                <label className="campo">
                  <span>Nome</span>
                  <input
                    value={nome}
                    onChange={(evento) => setNome(evento.target.value)}
                    autoComplete="off"
                    required
                  />
                </label>
              </div>

              <div className="login__atalhos">
                {CONTAS_DEMO.map((conta) => (
                  <button
                    key={conta.steamId}
                    type="button"
                    className="chip chip--acao"
                    onClick={() => {
                      setSteamId(conta.steamId)
                      setNome(conta.nome)
                    }}
                  >
                    {conta.nome}
                  </button>
                ))}
              </div>

              <button type="submit" className="botao botao--largo" disabled={enviando}>
                {enviando ? 'Entrando…' : 'Entrar como teste'}
              </button>
            </form>

            <p className="login__dica">
              Esse atalho só existe com <code>APP_DEV_LOGIN_ENABLED=true</code> na API.
            </p>
          </>
        )}
      </section>
    </main>
  )
}
