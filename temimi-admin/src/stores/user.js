import { defineStore } from 'pinia'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

export const useUserStore = defineStore('user', {
  state: () => ({
    // 是否加载中
    isLoading: false,
    // 是否登录
    isLogin: false,
    // 当前用户
    user: {},
  }),

  getters: {
    userId: (state) => state.user?.uid,
    username: (state) => state.user?.username,
    avatar: (state) => state.user?.avatar,
    token: () => localStorage.getItem('teri_token'),
  },

  actions: {
    // 初始化数据（退出登录或登录过期时）
    initData() {
      this.isLogin = false
      this.user = {}
    },

    // 更新用户信息
    updateUser(user) {
      this.user = user
    },

    // 更新登录状态
    updateIsLogin(isLogin) {
      this.isLogin = isLogin
    },

    // 获取当前用户信息
    async getPersonalInfo() {
      try {
        const result = await axios.get('/api/admin/account/info', {
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code !== 200) {
          // 认证失败，清除缓存
          this.initData()
          localStorage.removeItem('teri_token')
          ElMessage.error(result.data.message)
          router.push('/login')
          return
        }

        if (result.data.code === 200) {
          this.updateUser(result.data.data)
          this.isLogin = true
        }
      } catch (error) {
        // token失效，清空数据
        this.initData()
        localStorage.removeItem('teri_token')
        ElMessage.error('请登录后查看')
        router.push('/login')
      }
    },

    // 登录
    async login(credentials) {
      try {
        const result = await axios.post('/api/admin/account/login', credentials)
        if (result.data.code === 200) {
          const token = result.data.data.token
          localStorage.setItem('teri_token', token)
          await this.getPersonalInfo()
          return result.data
        } else {
          ElMessage.error(result.data.message || '登录失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        throw error
      }
    },

    // 退出登录
    logout() {
      // 先修改状态再发送请求，防止token过期导致退出失败
      this.initData()
      router.push('/login')
      
      // 发送退出请求，处理redis中的缓存信息
      axios.get('/api/admin/account/logout', {
        headers: {
          Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
        },
      })
      
      // 清除本地token缓存
      localStorage.removeItem('teri_token')
    },
  },
})
