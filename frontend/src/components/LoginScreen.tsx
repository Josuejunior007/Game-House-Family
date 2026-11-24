import { useAuth } from '../auth'
import { IconeSteam } from './Icons'

/**
 * Login só pela Steam. O SteamID64 e a família vêm do /auth/me depois do retorno da Steam —
 * não há formulário de nome/ID: era um atalho de teste que confundia com um login paralelo.
 */
export function LoginScreen() {
  const { entrarComSteam, erroConexao } = useAuth()

  return (
    <main className="login">
      <section className="login__cartao">
        <span className="login__marca" aria-hidden="true">
          <IconeSteam size={30} />
        </span>

        <h1>Game House Family</h1>
        <p className="login__subtitulo">
          Entre com a Steam para ver a sua família, adicionar membros pelo SteamID e combinar os jogos da casa.
        </p>

        {erroConexao && (
          <div className="alerta alerta--erro alerta--bloco" role="alert">
            <strong>Não consegui falar com a API.</strong>
            <span>{erroConexao}</span>
            <span className="alerta__dica">
              Confira se a API está rodando e se o CORS libera esta origem (<code>APP_FRONTEND_URL</code> na API).
            </span>
          </div>
        )}

        <button type="button" className="botao botao--steam botao--largo" onClick={entrarComSteam}>
          <IconeSteam size={20} />
          Entrar com a Steam
        </button>

        <p className="login__dica">
          Quem ainda não foi adicionado pelo dono consegue entrar, mas só vê o perfil e os convites.
        </p>
      </section>
    </main>
  )
}
