import { defineStore } from 'pinia'
import axios from 'axios'
import { ElMessage } from 'element-plus'

export const useCommentStore = defineStore('comment', {
  state: () => ({
    // 评论列表
    commentList: [],
    // 加载状态
    loading: false,
    // 分页信息
    pagination: {
      page: 1,
      pageSize: 20,
      total: 0,
    },
  }),

  getters: {
    commentCount: (state) => state.commentList.length,
    hasMore: (state) => {
      return state.commentList.length < state.pagination.total
    },
  },

  actions: {
    // 获取评论列表（管理员）
    async fetchCommentList(videoId, params = {}) {
      this.loading = true
      try {
        const result = await axios.get(`/api/admin/comment/${videoId}`, {
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
          this.commentList = data.list || []
          this.pagination.total = data.total || 0
          return data
        } else {
          ElMessage.error(result.data.message || '获取评论失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取评论失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 删除评论
    async deleteComment(commentId) {
      try {
        const result = await axios.delete(`/api/admin/comment/${commentId}`, {
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code === 200) {
          ElMessage.success('删除成功')
          // 从列表中移除
          const index = this.commentList.findIndex((c) => c.id === commentId)
          if (index !== -1) {
            this.commentList.splice(index, 1)
            this.pagination.total = Math.max(0, this.pagination.total - 1)
          }
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '删除失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('删除评论失败:', error)
        throw error
      }
    },

    // 批量删除评论
    async batchDeleteComment(commentIds) {
      try {
        const result = await axios.post(
          '/api/admin/comment/batch-delete',
          { ids: commentIds },
          {
            headers: {
              Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
            },
          }
        )

        if (result.data.code === 200) {
          ElMessage.success('批量删除成功')
          // 从列表中移除
          this.commentList = this.commentList.filter(
            (c) => !commentIds.includes(c.id)
          )
          this.pagination.total = Math.max(
            0,
            this.pagination.total - commentIds.length
          )
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '批量删除失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('批量删除评论失败:', error)
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

    // 清空评论列表
    clearCommentList() {
      this.commentList = []
      this.resetPagination()
    },
  },
})
