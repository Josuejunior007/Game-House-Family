import { useState } from 'react'
import { AuthProvider, useAuth } from './auth'
import { CatalogView } from './components/CatalogView'
import { Header, type Aba } from './components/Header'
import { LoginScreen } from './components/LoginScreen'
import { SuggestionsView } from './components/SuggestionsView'
import { ToastProvider } from './components/Toast'

function Aplicacao() {
  const { usuario, carregando } = useAuth()
  const [aba, setAba] = useState<Aba>('sugestoes')

  if (carregando) {
    return (
      <div className="tela-carregando">
        <span className="spinner" />
        <span>Carregando…</span>
      </div>
    )
  }

  if (!usuario) return <LoginScreen />

  return (
    <div className="app">
      <Header aba={aba} onMudarAba={setAba} />
      <main className="conteudo">{aba === 'sugestoes' ? <SuggestionsView usuario={usuario} /> : <CatalogView />}</main>
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
