// src/utils/request.js

import axios from 'axios'

// 创建一个axios实例
const request = axios.create({
  baseURL: '/api', // 通过代理访问后端
  timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从Pinia中获取Token
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data // 返回成功的数据
    } else {
      // 处理业务错误
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
  },
  (error) => {
    // 处理网络错误或HTTP状态码非2xx
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request