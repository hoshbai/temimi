import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      'assets': resolve(__dirname, 'src/assets'),
      'components': resolve(__dirname, 'src/components'),
      'network': resolve(__dirname, 'src/network'),
      'views': resolve(__dirname, 'src/views'),
      'utils': resolve(__dirname, 'src/utils')
    }
  },
  
  server: {
    port: 8788,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      // 静态资源代理
      '/avatars': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/covers': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/videos': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/backgrounds': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'video-player': ['video.js'],
          'charts': ['echarts']
        }
      }
    }
  },
  
  test: {
    globals: true,
    environment: 'happy-dom',
    setupFiles: ['./src/tests/setup.js']
  }
})
