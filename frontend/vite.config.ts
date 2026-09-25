import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Repassa /api para o Spring Boot, assim não precisa de CORS em desenvolvimento
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
