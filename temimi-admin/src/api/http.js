import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

// Create axios instance
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080',
  timeout: 30000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor - Add JWT token
http.interceptors.request.use(
  config => {
    const token = localStorage.getItem('teri_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// Response interceptor - Handle errors and responses
http.interceptors.response.use(
  response => {
    // Check for API response code
    const code = response.data?.code
    if (code && code !== 200) {
      ElMessage.error(response.data.message || '未知错误')
      return Promise.reject(new Error(response.data.message || '请求失败'))
    }
    return response
  },
  error => {
    console.error('Response error:', error)
    
    if (error.response) {
      const { status, headers, data } = error.response
      const userStore = useUserStore()
      
      switch (status) {
        case 401:
          // Unauthorized - token expired or invalid
          if (headers.message === 'not login' || data?.message?.includes('登录')) {
            userStore.initData()
            localStorage.removeItem('teri_token')
            ElMessage.error('请登录后查看')
            router.push('/login')
          } else {
            ElMessage.error('认证失败，请重新登录')
            router.push('/login')
          }
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(data?.message || '请求失败')
      }
    } else if (error.request) {
      // Request was made but no response received
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      // Something else happened
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default http
