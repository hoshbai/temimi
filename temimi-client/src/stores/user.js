import { defineStore } from 'pinia'
import axios from 'axios'
import http from '@/api/http'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  state: () => ({
    // 是否加载中
    isLoading: false,
    // 是否登录
    isLogin: false,
    // 是否外部触发打开登录框
    openLogin: false,
    // 当前用户
    user: {},
    // 未读消息数 分别对应"reply"/"at"/"love"/"system"/"whisper"/"dynamic"
    msgUnread: [0, 0, 0, 0, 0, 0],
    // 聊天列表
    chatList: [],
    // 当前聊天对象的uid (不是聊天的id)
    chatId: -1,
    // 当前页面是否在聊天界面
    isChatPage: false,
    // 实时通讯的socket
    ws: null,
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
      this.msgUnread = [0, 0, 0, 0, 0, 0]
      this.chatList = []
      this.chatId = -1
    },

    // 更新用户信息
    updateUser(user) {
      this.user = user
    },

    // 更新登录状态
    updateIsLogin(isLogin) {
      this.isLogin = isLogin
    },

    // 追加更新聊天列表
    updateChatList(chatList) {
      this.chatList.push(...chatList)
    },

    // 获取当前用户信息
    async getPersonalInfo() {
      try {
        const result = await http.get('/api/user/personal/info')

        if (result.data.code === 200) {
          const userData = result.data.data
          // 字段名映射：后端返回 avatar，前端使用 avatar_url
          if (userData.avatar && !userData.avatar_url) {
            userData.avatar_url = userData.avatar
          }
          this.updateUser(userData)
          this.isLogin = true
          
          // ✅ 获取用户信息成功后，自动尝试领取每日登录奖励
          this.claimDailyReward()
        }
      } catch (error) {
        // token失效，清空数据
        this.initData()
        // 关闭websocket
        if (this.ws) {
          this.ws.close()
          this.ws = null
        }
        // 清除本地token缓存
        localStorage.removeItem('teri_token')
        ElMessage.error('请登录后查看')
      }
    },

    // 登录
    async login(credentials) {
      try {
        const result = await http.post('/api/user/account/login', credentials)
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

    // 注册
    async register(userData) {
      try {
        const result = await http.post('/api/user/account/register', userData)
        if (result.data.code === 200) {
          return result.data
        } else {
          ElMessage.error(result.data.message || '注册失败')
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
      // 关闭websocket
      if (this.ws) {
        this.ws.close()
        this.ws = null
      }
      // 发送退出请求，处理redis中的缓存信息
      http
        .get('/api/user/account/logout')
        .catch(() => {})
      // 清除本地token缓存
      localStorage.removeItem('teri_token')
    },

    // 获取全部未读消息数
    async getMsgUnread() {
      try {
        const result = await http.get('/api/msg-unread/all')
        if (result.data.code === 200) {
          const data = result.data.data
          this.msgUnread[0] = data.reply
          this.msgUnread[1] = data.at
          this.msgUnread[2] = data.love
          this.msgUnread[3] = data.system
          this.msgUnread[4] = data.whisper
          this.msgUnread[5] = data.dynamic
        }
      } catch (error) {
        console.error('获取未读消息数失败:', error)
      }
    },

    // 初始化websocket实例
    connectWebSocket() {
      return new Promise((resolve) => {
        if (this.ws) {
          this.ws.close()
          this.ws = null
        }
        const wsBaseUrl = import.meta.env.VITE_WS_IM_URL || 'ws://localhost:8080'
        const token = localStorage.getItem('teri_token')
        const ws = new WebSocket(`${wsBaseUrl}/im?token=${token}`)
        this.ws = ws

        ws.addEventListener('open', () => {
          console.log('实时通信websocket已建立')
          resolve()
        })

        ws.addEventListener('close', () => {
          this.handleWsClose()
        })

        ws.addEventListener('message', (e) => {
          this.handleWsMessage(e)
        })

        ws.addEventListener('error', (e) => {
          console.log('实时通信websocket报错: ', e)
        })
      })
    },

    // 关闭websocket
    async closeWebSocket() {
      if (this.ws) {
        await this.ws.close()
        this.ws = null
      }
    },

    // 处理websocket关闭
    handleWsClose() {
      console.log('实时通信websocket已关闭')
      // ✅ 修复：不应该在WebSocket关闭时清空登录状态
      // WebSocket关闭可能是由于网络波动、页面切换等正常原因
      // 只有在token真正失效时才应该清空状态（在getPersonalInfo的catch中处理）

      // 只清空WebSocket引用，保留用户登录状态
      this.ws = null
    },

    // 处理websocket消息
    handleWsMessage(e) {
      const data = JSON.parse(e.data)
      
      switch (data.type) {
        case 'error': {
          if (data.data === '登录已过期') {
            this.initData()
            localStorage.removeItem('teri_token')
          }
          ElMessage.error(data.data)
          break
        }
        case 'reply': {
          const content = data.data
          if (content.type === '全部已读') {
            this.msgUnread[0] = 0
          } else if (content.type === '接收') {
            this.msgUnread[0]++
          }
          break
        }
        case 'at': {
          const content = data.data
          if (content.type === '全部已读') {
            this.msgUnread[1] = 0
          } else if (content.type === '接收') {
            this.msgUnread[1]++
          }
          break
        }
        case 'love': {
          const content = data.data
          if (content.type === '全部已读') {
            this.msgUnread[2] = 0
          } else if (content.type === '接收') {
            this.msgUnread[2]++
          }
          break
        }
        case 'system': {
          const content = data.data
          if (content.type === '全部已读') {
            this.msgUnread[3] = 0
          } else if (content.type === '接收') {
            this.msgUnread[3]++
          }
          break
        }
        case 'whisper': {
          this.handleWhisperMessage(data.data)
          break
        }
        case 'dynamic': {
          const content = data.content
          if (content.type === '全部已读') {
            this.msgUnread[5] = 0
          } else if (content.type === '接收') {
            this.msgUnread[5]++
          }
          break
        }
      }
    },

    // 处理私聊消息
    handleWhisperMessage(content) {
      const sortByLatestTime = (list) => {
        list.sort((a, b) => {
          const timeA = new Date(a.chat.latestTime).getTime()
          const timeB = new Date(b.chat.latestTime).getTime()
          return timeB - timeA
        })
      }

      switch (content.type) {
        case '全部已读': {
          this.msgUnread[4] = 0
          this.chatList.forEach((item) => {
            item.chat.unread = 0
          })
          break
        }
        case '已读': {
          const chatid = content.id
          const count = content.count
          this.msgUnread[4] = Math.max(0, this.msgUnread[4] - count)
          const chat = this.chatList.find((item) => item.chat.id === chatid)
          if (chat) {
            chat.chat.unread = 0
          }
          break
        }
        case '移除': {
          const chatid = content.id
          const count = content.count
          this.msgUnread[4] = Math.max(0, this.msgUnread[4] - count)
          const i = this.chatList.findIndex((item) => item.chat.id === chatid)
          if (i !== -1) {
            if (this.chatList[i].user.uid === this.chatId) this.chatId = -1
            this.chatList.splice(i, 1)
          }
          break
        }
        case '接收': {
          const chat = content.chat
          const detail = content.detail
          const user = content.user

          if (detail.userId === this.user.uid) {
            // 发送方是自己 - 查找对方的聊天项（通过对方的 uid）
            let chatItem = this.chatList.find(
              (item) => item && item.user && item.user.uid === detail.anotherId
            )
            if (chatItem) {
              // 如果找到了聊天项，添加消息
              if (this.isChatPage) {
                chatItem.detail.list.push(detail)
              }
              if (chat && chat.latestTime) {
                chatItem.chat.latestTime = chat.latestTime
              }
              sortByLatestTime(this.chatList)
            } else {
              // 如果没找到聊天项，创建一个新的
              chatItem = {
                chat: chat,
                user: user,
                detail: {
                  more: true,
                  list: [],
                },
              }
              if (this.isChatPage) {
                chatItem.detail.list.push(detail)
              }
              this.chatList.unshift(chatItem)
            }
          } else {
            // 发送方是别人 - 查找发送方的聊天项（通过发送方的 uid）
            if (!content.online) {
              this.msgUnread[4]++
            }
            let chatItem = this.chatList.find(
              (item) => item && item.user && item.user.uid === detail.userId
            )
            if (chatItem) {
              chatItem.detail.list.push(detail)
              chatItem.chat = chat
              sortByLatestTime(this.chatList)
            } else {
              chatItem = {
                chat: chat,
                user: user,
                detail: {
                  more: true,
                  list: [],
                },
              }
              chatItem.detail.list.push(detail)
              this.chatList.unshift(chatItem)
            }
          }
          break
        }
        case '撤回': {
          const msgId = content.id
          const sendId = content.sendId
          const acceptId = content.acceptId
          let chat
          if (sendId === this.user.uid) {
            chat = this.chatList.find((item) => item.chat.userId === acceptId)
          } else {
            chat = this.chatList.find((item) => item.chat.userId === sendId)
          }
          if (chat) {
            const msg = chat.detail.list.find((item) => item.id === msgId)
            if (msg) {
              msg.withdraw = 1
            }
          }
          break
        }
      }
    },

    // 获取用户资料
    async fetchUserProfile(uid) {
      try {
        const result = await http.get(`/api/user/profile/${uid}`)

        if (result.data.code === 200) {
          return result.data.data
        } else {
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取用户资料失败:', error)
        throw error
      }
    },

    // 获取用户收藏夹列表
    async fetchUserFavorites(uid) {
      try {
        const result = await http.get(`/api/favorite/user/${uid}`)

        if (result.data.code === 200) {
          return result.data.data
        } else {
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取用户收藏夹失败:', error)
        throw error
      }
    },

    // 获取用户硬币余额
    async fetchUserCoins() {
      try {
        const result = await http.get('/api/coin/balance')

        if (result.data.code === 200) {
          return result.data.data
        } else {
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取硬币余额失败:', error)
        throw error
      }
    },

    // 领取每日登录奖励
    async claimDailyReward() {
      try {
        // 先检查今日是否已登录
        const checkResult = await http.get('/api/coin/check-today-login')
        
        if (checkResult.data.code === 200 && checkResult.data.data === false) {
          // 今日未登录，尝试领取奖励
          const result = await http.post('/api/coin/daily-reward')
          
          if (result.data.code === 200) {
            // 静默领取，不显示提示（或者可以显示一个小提示）
            console.log('每日登录奖励已领取')
            // 可选：显示一个不打扰的提示
            // ElMessage.success('获得每日登录奖励 1 硬币', { duration: 2000 })
          }
        }
      } catch (error) {
        // 静默失败，不影响用户体验
        console.error('领取每日奖励失败:', error)
      }
    },
  },
})
