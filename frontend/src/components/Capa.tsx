import { useState } from 'react'

interface Props {
  src: string
  alt: string
  /** Nome mostrado no lugar da imagem quando a Steam não tem capa para este appId. */
  nome: string
  className?: string
}

/** Imagem de capa com fallback: alguns appIds não têm header.jpg e o <img> quebraria. */
export function Capa({ src, alt, nome, className }: Props) {
  const [falhou, setFalhou] = useState(false)

  if (falhou) {
    return (
      <div className={className ? `${className} capa-falhou` : 'capa-falhou'} role="img" aria-label={alt}>
        <svg
          viewBox="0 0 24 24"
          width="20"
          height="20"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.6"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <rect x="3" y="5" width="18" height="14" rx="2.5" />
          <path d="m3.5 15.5 4-4 3.5 3.5 3-3 6 6" />
          <circle cx="9" cy="9.5" r="1.1" />
        </svg>
        <span>{nome}</span>
      </div>
    )
  }

  return <img className={className} src={src} alt={alt} loading="lazy" onError={() => setFalhou(true)} />
}
