// src/stores/user.js

import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    // 用户信息
    userInfo: null,
    // JWT Token
    token: localStorage.getItem('token') || null,
  }),

  getters: {
    // 判断用户是否已登录
    isLoggedIn: (state) => !!state.token,
    // 获取用户ID
    uid: (state) => state.userInfo?.uid || null,
    // 获取用户昵称
    nickname: (state) => state.userInfo?.nickname || '',
    // 获取用户头像
    avatar: (state) => state.userInfo?.avatar || '/default_avatar.png',
  },

  actions: {
    // 设置用户信息和Token
    setUserInfo(userInfo, token) {
      this.userInfo = userInfo
      this.token = token
      localStorage.setItem('token', token) // 持久化Token
    },
    // 清除用户信息和Token (退出登录)
    clearUserInfo() {
      this.userInfo = null
      this.token = null
      localStorage.removeItem('token')
    }
  }
})