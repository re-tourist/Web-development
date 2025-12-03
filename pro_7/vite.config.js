import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite dev server with CORS and proxy to backend
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5177,
    cors: true,
    // Proxy /api/* to Spring Boot backend at http://localhost:8082
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
        // keep path as /api/*
        rewrite: (path) => path.replace(/^\/api/, '/api'),
      },
    },
  },
})
