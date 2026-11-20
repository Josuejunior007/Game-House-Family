import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// A porta 5173 é a mesma usada em APP_FRONTEND_URL, então o login Steam devolve
// o usuário para cá e o CORS do back-end já libera esta origem.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
  },
  preview: {
    port: 5173,
    strictPort: true,
  },
})
