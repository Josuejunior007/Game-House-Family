/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** URL da API. Padrão: http://localhost:8081 */
  readonly VITE_API_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
