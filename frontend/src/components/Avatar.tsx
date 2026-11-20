import { iniciais } from '../format'

interface Props {
  nome: string
  url?: string | null
  tamanho?: number
}

/** Foto da Steam ou, sem foto, as iniciais (login de dev não tem avatar). */
export function Avatar({ nome, url, tamanho = 32 }: Props) {
  if (url) {
    return (
      <img
        className="avatar"
        src={url}
        alt=""
        width={tamanho}
        height={tamanho}
        style={{ width: tamanho, height: tamanho }}
        loading="lazy"
      />
    )
  }

  return (
    <span
      className="avatar avatar--iniciais"
      style={{ width: tamanho, height: tamanho, fontSize: Math.round(tamanho * 0.38) }}
      aria-hidden="true"
    >
      {iniciais(nome)}
    </span>
  )
}
