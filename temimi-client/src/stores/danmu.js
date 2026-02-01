import { defineStore } from 'pinia'
import axios from 'axios'
import { ElMessage } from 'element-plus'

export const useDanmuStore = defineStore('danmu', {
  state: () => ({
    // 弹幕列表
    danmuList: [],
    // 加载状态
    loading: false,
    // WebSocket连接状态
    wsConnected: false,
    // WebSocket实例
    danmuWs: null,
  }),

  getters: {
    danmuCount: (state) => state.danmuList.length,
  },

  actions: {
    // 更新弹幕列表
    updateDanmuList(danmuList) {
      this.danmuList = danmuList
    },

    // 添加弹幕到列表
    addDanmu(danmu) {
      this.danmuList.push(danmu)
    },

    // 从列表中删除弹幕
    removeDanmu(danmuId) {
      const index = this.danmuList.findIndex(d => d.id === danmuId)
      if (index !== -1) {
        this.danmuList.splice(index, 1)
        console.log(`弹幕 ${danmuId} 已删除`)
      }
    },

    // 获取视频弹幕列表
    async fetchDanmuList(videoId) {
      this.loading = true
      try {
        const result = await axios.get(`/api/danmu/${videoId}`, {
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code === 200) {
          this.danmuList = result.data.data || []
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '获取弹幕失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取弹幕失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 发送弹幕
    async postDanmu(videoId, danmuData) {
      try {
        const result = await axios.post(
          `/api/danmu/${videoId}`,
          danmuData,
          {
            headers: {
              Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
            },
          }
        )

        if (result.data.code === 200) {
          const newDanmu = result.data.data
          this.addDanmu(newDanmu)
          return newDanmu
        } else {
          ElMessage.error(result.data.message || '发送弹幕失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('发送弹幕失败:', error)
        throw error
      }
    },

    // 连接弹幕WebSocket
    connectDanmuWebSocket(videoId) {
      return new Promise((resolve, reject) => {
        if (this.danmuWs) {
          this.danmuWs.close()
          this.danmuWs = null
        }

        const wsBaseUrl = import.meta.env.VITE_WS_DANMU_URL || 'ws://localhost:8080'
        const ws = new WebSocket(`${wsBaseUrl}/danmu/${videoId}`)
        this.danmuWs = ws

        ws.addEventListener('open', () => {
          console.log('弹幕WebSocket已连接')
          this.wsConnected = true
          resolve()
        })

        ws.addEventListener('close', () => {
          console.log('弹幕WebSocket已关闭')
          this.wsConnected = false
        })

        ws.addEventListener('message', (e) => {
          try {
            const message = JSON.parse(e.data)
            
            // 检查消息类型
            if (message.type === 'delete') {
              // 删除弹幕消息
              this.removeDanmu(message.danmuId)
            } else if (typeof message === 'string') {
              // 观看人数等文本消息
              console.log('WebSocket消息:', message)
            } else {
              // 新弹幕消息
              this.addDanmu(message)
            }
          } catch (error) {
            console.error('解析弹幕数据失败:', error)
          }
        })

        ws.addEventListener('error', (e) => {
          console.error('弹幕WebSocket错误:', e)
          this.wsConnected = false
          reject(e)
        })
      })
    },

    // 关闭弹幕WebSocket
    closeDanmuWebSocket() {
      if (this.danmuWs) {
        this.danmuWs.close()
        this.danmuWs = null
        this.wsConnected = false
      }
    },

    // 通过WebSocket发送弹幕
    sendDanmuViaWebSocket(danmuData) {
      if (this.danmuWs && this.wsConnected) {
        this.danmuWs.send(JSON.stringify(danmuData))
      } else {
        ElMessage.error('弹幕连接未建立')
      }
    },

    // 清空弹幕列表
    clearDanmuList() {
      this.danmuList = []
    },
  },
})
