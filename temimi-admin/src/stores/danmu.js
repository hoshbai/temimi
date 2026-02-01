import { defineStore } from 'pinia'
import axios from 'axios'
import { ElMessage } from 'element-plus'

export const useDanmuStore = defineStore('danmu', {
  state: () => ({
    // 弹幕列表
    danmuList: [],
    // 加载状态
    loading: false,
  }),

  getters: {
    danmuCount: (state) => state.danmuList.length,
  },

  actions: {
    // 获取视频弹幕列表（管理员）
    async fetchDanmuList(videoId) {
      this.loading = true
      try {
        const result = await axios.get(`/api/admin/danmu/${videoId}`, {
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

    // 删除弹幕
    async deleteDanmu(danmuId) {
      try {
        const result = await axios.delete(`/api/admin/danmu/${danmuId}`, {
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code === 200) {
          ElMessage.success('删除成功')
          // 从列表中移除
          const index = this.danmuList.findIndex((d) => d.id === danmuId)
          if (index !== -1) {
            this.danmuList.splice(index, 1)
          }
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '删除失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('删除弹幕失败:', error)
        throw error
      }
    },

    // 批量删除弹幕
    async batchDeleteDanmu(danmuIds) {
      try {
        const result = await axios.post(
          '/api/admin/danmu/batch-delete',
          { ids: danmuIds },
          {
            headers: {
              Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
            },
          }
        )

        if (result.data.code === 200) {
          ElMessage.success('批量删除成功')
          // 从列表中移除
          this.danmuList = this.danmuList.filter(
            (d) => !danmuIds.includes(d.id)
          )
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '批量删除失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('批量删除弹幕失败:', error)
        throw error
      }
    },

    // 清空弹幕列表
    clearDanmuList() {
      this.danmuList = []
    },
  },
})
