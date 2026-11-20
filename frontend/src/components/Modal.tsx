import { useEffect, type ReactNode } from 'react'
import { IconeX } from './Icons'

interface Props {
  aberto: boolean
  titulo: string
  onFechar: () => void
  children: ReactNode
  largura?: number
}

export function Modal({ aberto, titulo, onFechar, children, largura = 580 }: Props) {
  useEffect(() => {
    if (!aberto) return

    const aoTeclar = (evento: KeyboardEvent) => {
      if (evento.key === 'Escape') onFechar()
    }

    const overflowAnterior = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    document.addEventListener('keydown', aoTeclar)

    return () => {
      document.removeEventListener('keydown', aoTeclar)
      document.body.style.overflow = overflowAnterior
    }
  }, [aberto, onFechar])

  if (!aberto) return null

  return (
    <div
      className="modal-fundo"
      onMouseDown={(evento) => {
        if (evento.target === evento.currentTarget) onFechar()
      }}
    >
      <div className="modal" role="dialog" aria-modal="true" aria-label={titulo} style={{ maxWidth: largura }}>
        <header className="modal__topo">
          <h2>{titulo}</h2>
          <button type="button" className="botao botao--icone" onClick={onFechar} aria-label="Fechar">
            <IconeX />
          </button>
        </header>
        <div className="modal__corpo">{children}</div>
      </div>
    </div>
  )
}
