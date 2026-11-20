import { useState } from 'react'
import { useAuth } from '../auth'
import { Avatar } from './Avatar'
import { IconeSair } from './Icons'

export type Aba = 'sugestoes' | 'catalogo'

interface Props {
  aba: Aba
  onMudarAba: (aba: Aba) => void
}

export function Header({ aba, onMudarAba }: Props) {
  const { usuario, sair } = useAuth()
  const [saindo, setSaindo] = useState(false)

  async function sairDaConta() {
    setSaindo(true)
    try {
      await sair()
    } finally {
      setSaindo(false)
    }
  }

  return (
    <header className="topo">
      <div className="topo__interno">
        <div className="topo__marca">
          <span className="logo" aria-hidden="true">
            <svg
              viewBox="0 0 24 24"
              width="22"
              height="22"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M7.5 8h9a4 4 0 0 1 3.9 3.1l.8 4A3 3 0 0 1 18.7 19l-1.5-2.2H6.8L5.3 19a3 3 0 0 1-3.5-3.9l.8-4A4 4 0 0 1 7.5 8Z" />
              <path d="M9.4 11.6v2.2M8.3 12.7h2.2M14.6 12.1h.01M16.2 13.8h.01" />
            </svg>
          </span>
          <span className="topo__titulo">
            Game House <strong>Family</strong>
          </span>
        </div>

        <nav className="abas" aria-label="Seções">
          <button
            type="button"
            className={`aba ${aba === 'sugestoes' ? 'aba--ativa' : ''}`}
            onClick={() => onMudarAba('sugestoes')}
            aria-current={aba === 'sugestoes' ? 'page' : undefined}
          >
            Sugestões
          </button>
          <button
            type="button"
            className={`aba ${aba === 'catalogo' ? 'aba--ativa' : ''}`}
            onClick={() => onMudarAba('catalogo')}
            aria-current={aba === 'catalogo' ? 'page' : undefined}
          >
            Catálogo
          </button>
        </nav>

        {usuario && (
          <div className="topo__usuario">
            <Avatar nome={usuario.displayName} url={usuario.avatarUrl} tamanho={30} />
            <span className="topo__nome">{usuario.displayName}</span>
            <button
              type="button"
              className="botao botao--icone"
              onClick={sairDaConta}
              disabled={saindo}
              aria-label="Sair"
              title="Sair"
            >
              <IconeSair />
            </button>
          </div>
        )}
      </div>
    </header>
  )
}
