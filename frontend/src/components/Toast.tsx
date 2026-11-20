import { createContext, useCallback, useContext, useMemo, useRef, useState, type ReactNode } from 'react'

type Tipo = 'sucesso' | 'erro' | 'info'

interface Aviso {
  id: number
  tipo: Tipo
  mensagem: string
}

interface ToastContextValue {
  sucesso: (mensagem: string) => void
  erro: (mensagem: string) => void
  info: (mensagem: string) => void
}

const ToastContext = createContext<ToastContextValue | null>(null)

export function ToastProvider({ children }: { children: ReactNode }) {
  const [avisos, setAvisos] = useState<Aviso[]>([])
  const proximoId = useRef(1)

  const remover = useCallback((id: number) => {
    setAvisos((atuais) => atuais.filter((aviso) => aviso.id !== id))
  }, [])

  const adicionar = useCallback(
    (tipo: Tipo, mensagem: string) => {
      const id = proximoId.current++
      setAvisos((atuais) => [...atuais, { id, tipo, mensagem }])
      window.setTimeout(() => remover(id), tipo === 'erro' ? 6500 : 3500)
    },
    [remover],
  )

  const valor = useMemo<ToastContextValue>(
    () => ({
      sucesso: (mensagem) => adicionar('sucesso', mensagem),
      erro: (mensagem) => adicionar('erro', mensagem),
      info: (mensagem) => adicionar('info', mensagem),
    }),
    [adicionar],
  )

  return (
    <ToastContext.Provider value={valor}>
      {children}
      <div className="toasts" role="status" aria-live="polite">
        {avisos.map((aviso) => (
          <button
            key={aviso.id}
            type="button"
            className={`toast toast--${aviso.tipo}`}
            onClick={() => remover(aviso.id)}
            title="Clique para dispensar"
          >
            {aviso.mensagem}
          </button>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast(): ToastContextValue {
  const contexto = useContext(ToastContext)
  if (!contexto) throw new Error('useToast precisa estar dentro de <ToastProvider>')
  return contexto
}
