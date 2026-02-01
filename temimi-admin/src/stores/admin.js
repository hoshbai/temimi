import { defineStore } from 'pinia'
import axios from 'axios'
import { ElMessage } from 'element-plus'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    // 加载状态
    isLoading: false,
    // 统计数据
    statistics: {
      totalUsers: 0,
      totalVideos: 0,
      totalComments: 0,
      totalDanmu: 0,
      pendingReviews: 0,
      todayNewUsers: 0,
      todayNewVideos: 0,
      todayViews: 0,
    },
    // 待审核视频列表
    pendingVideos: [],
    // 用户管理列表
    userList: [],
    // 用户分页
    userPagination: {
      page: 1,
      pageSize: 20,
      total: 0,
    },
  }),

  getters: {
    hasPendingReviews: (state) => state.statistics.pendingReviews > 0,
  },

  actions: {
    // 获取统计数据
    async fetchStatistics() {
      this.isLoading = true
      try {
        const result = await axios.get('/api/admin/statistics', {
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code === 200) {
          this.statistics = result.data.data
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '获取统计数据失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取统计数据失败:', error)
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // 获取待审核视频列表
    async fetchPendingVideos(params = {}) {
      this.isLoading = true
      try {
        const result = await axios.get('/api/admin/video/pending', {
          params,
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code === 200) {
          this.pendingVideos = result.data.data || []
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '获取待审核视频失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取待审核视频失败:', error)
        throw error
      } finally {
        this.isLoading = false
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
          // 从待审核列表中移除
          const index = this.pendingVideos.findIndex((v) => v.id === videoId)
          if (index !== -1) {
            this.pendingVideos.splice(index, 1)
          }
          // 更新统计数据
          if (this.statistics.pendingReviews > 0) {
            this.statistics.pendingReviews--
          }
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

    // 获取用户列表
    async fetchUserList(params = {}) {
      this.isLoading = true
      try {
        const result = await axios.get('/api/admin/user/list', {
          params: {
            page: this.userPagination.page,
            pageSize: this.userPagination.pageSize,
            ...params,
          },
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code === 200) {
          const data = result.data.data
          this.userList = data.list || []
          this.userPagination.total = data.total || 0
          return data
        } else {
          ElMessage.error(result.data.message || '获取用户列表失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取用户列表失败:', error)
        throw error
      } finally {
        this.isLoading = false
      }
    },

    // 封禁用户
    async banUser(userId, reason = '', duration = 0) {
      try {
        const result = await axios.post(
          `/api/admin/user/${userId}/ban`,
          { reason, duration },
          {
            headers: {
              Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
            },
          }
        )

        if (result.data.code === 200) {
          ElMessage.success('封禁成功')
          // 更新用户列表中的状态
          const user = this.userList.find((u) => u.id === userId)
          if (user) {
            user.status = 'banned'
          }
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '封禁失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('封禁用户失败:', error)
        throw error
      }
    },

    // 解封用户
    async unbanUser(userId) {
      try {
        const result = await axios.post(
          `/api/admin/user/${userId}/unban`,
          {},
          {
            headers: {
              Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
            },
          }
        )

        if (result.data.code === 200) {
          ElMessage.success('解封成功')
          // 更新用户列表中的状态
          const user = this.userList.find((u) => u.id === userId)
          if (user) {
            user.status = 'active'
          }
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '解封失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('解封用户失败:', error)
        throw error
      }
    },

    // 删除用户
    async deleteUser(userId) {
      try {
        const result = await axios.delete(`/api/admin/user/${userId}`, {
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code === 200) {
          ElMessage.success('删除成功')
          // 从列表中移除
          const index = this.userList.findIndex((u) => u.id === userId)
          if (index !== -1) {
            this.userList.splice(index, 1)
            this.userPagination.total = Math.max(
              0,
              this.userPagination.total - 1
            )
          }
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '删除失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('删除用户失败:', error)
        throw error
      }
    },

    // 获取系统日志
    async fetchSystemLogs(params = {}) {
      try {
        const result = await axios.get('/api/admin/logs', {
          params,
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code === 200) {
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '获取日志失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取系统日志失败:', error)
        throw error
      }
    },

    // 设置用户分页
    setUserPagination(page, pageSize) {
      this.userPagination.page = page
      if (pageSize) {
        this.userPagination.pageSize = pageSize
      }
    },

    // 重置用户分页
    resetUserPagination() {
      this.userPagination.page = 1
      this.userPagination.total = 0
    },
  },
})
