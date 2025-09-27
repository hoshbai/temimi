import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 3000, // 前端服务端口
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 您的后端服务地址
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '') // 将 /api/user/login 重写为 /user/login
      }
    }
  }
})