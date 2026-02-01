import { defineStore } from 'pinia'
import axios from 'axios'
import { ElMessage } from 'element-plus'

export const useVideoStore = defineStore('video', {
  state: () => ({
    // 视频列表
    videoList: [],
    // 当前视频详情
    currentVideo: null,
    // 加载状态
    loading: false,
    // 分页信息
    pagination: {
      page: 1,
      pageSize: 20,
      total: 0,
    },
    // 分区列表
    channels: [],
  }),

  getters: {
    hasMore: (state) => {
      return state.videoList.length < state.pagination.total
    },
  },

  actions: {
    // 更新分区列表
    updateChannels(channels) {
      this.channels = channels
    },

    // 获取视频列表（管理员）
    async fetchVideoList(params = {}) {
      this.loading = true
      try {
        const result = await axios.get('/api/admin/video/list', {
          params: {
            page: this.pagination.page,
            pageSize: this.pagination.pageSize,
            ...params,
          },
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code === 200) {
          const data = result.data.data
          this.videoList = data.list || []
          this.pagination.total = data.total || 0
          return data
        } else {
          ElMessage.error(result.data.message || '获取视频列表失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取视频列表失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 获取视频详情
    async fetchVideoDetail(videoId) {
      this.loading = true
      try {
        const result = await axios.get(`/api/admin/video/${videoId}`, {
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code === 200) {
          this.currentVideo = result.data.data
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '获取视频详情失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取视频详情失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 审核视频
    async reviewVideo(videoId, status, reason = '') {
      try {
        const result = await axios.post(
          `/api/admin/video/${videoId}/review`,
          { status, reason },
          {
            headers: {
              Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
            },
          }
        )

        if (result.data.code === 200) {
          ElMessage.success('审核成功')
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '审核失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('审核失败:', error)
        throw error
      }
    },

    // 删除视频
    async deleteVideo(videoId) {
      try {
        const result = await axios.delete(`/api/admin/video/${videoId}`, {
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code === 200) {
          ElMessage.success('删除成功')
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '删除失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('删除失败:', error)
        throw error
      }
    },

    // 设置分页
    setPagination(page, pageSize) {
      this.pagination.page = page
      if (pageSize) {
        this.pagination.pageSize = pageSize
      }
    },

    // 重置分页
    resetPagination() {
      this.pagination.page = 1
      this.pagination.total = 0
    },
  },
})
