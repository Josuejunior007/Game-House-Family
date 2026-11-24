import { useCallback, useEffect, useState } from 'react'
import { api } from './api'
import { AuthProvider, useAuth } from './auth'
import { CatalogView } from './components/CatalogView'
import { FamilyView } from './components/FamilyView'
import { Header, type Aba } from './components/Header'
import { InvitesView } from './components/InvitesView'
import { LoginScreen } from './components/LoginScreen'
import { SuggestionsView } from './components/SuggestionsView'
import { ToastProvider } from './components/Toast'

function Aplicacao() {
  const { usuario, carregando, recarregar } = useAuth()
  const [aba, setAba] = useState<Aba>('sugestoes')
  const [convitesPendentes, setConvitesPendentes] = useState(0)

  const atualizarConvites = useCallback(async () => {
    try {
      const lista = await api.meusConvites()
      setConvitesPendentes(lista.length)
    } catch {
      setConvitesPendentes(0)
    }
  }, [])

  useEffect(() => {
    if (!usuario) {
      setConvitesPendentes(0)
      return
    }
    void atualizarConvites()
  }, [usuario, atualizarConvites])

  // Quem está sem família fica na aba Família (onboarding). Sem isso, ao criar a família
  // o app pularia para "Sugestões" e o dono perderia de vista o formulário de convite.
  useEffect(() => {
    if (usuario && usuario.familyId == null) {
      setAba('familia')
    }
  }, [usuario])

  if (carregando) {
    return (
      <div className="tela-carregando">
        <span className="spinner" />
        <span>Carregando…</span>
      </div>
    )
  }

  if (!usuario) return <LoginScreen />

  const temFamilia = usuario.familyId != null
  // Quem ainda não entrou em uma família não tem o que fazer nas sugestões: cai no onboarding.
  const abaVisivel: Aba = !temFamilia && (aba === 'sugestoes' || aba === 'catalogo') ? 'familia' : aba

  return (
    <div className="app">
      <Header
        aba={abaVisivel}
        onMudarAba={setAba}
        temFamilia={temFamilia}
        convitesPendentes={convitesPendentes}
      />
      <main className="conteudo">
        {abaVisivel === 'sugestoes' && <SuggestionsView usuario={usuario} />}
        {abaVisivel === 'catalogo' && <CatalogView />}
        {abaVisivel === 'familia' && (
          <FamilyView onAtualizar={recarregar} onAtualizarConvites={atualizarConvites} />
        )}
        {abaVisivel === 'convites' && (
          <InvitesView
            onAceito={async () => {
              await recarregar()
              await atualizarConvites()
              setAba('familia')
            }}
            onMudou={atualizarConvites}
          />
        )}
      </main>
      <footer className="rodape">
        <span>Game House Family</span>
        <span className="muted">Capas e preços vêm da Steam.</span>
      </footer>
    </div>
  )
}

export default function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <Aplicacao />
      </AuthProvider>
    </ToastProvider>
  )
}
