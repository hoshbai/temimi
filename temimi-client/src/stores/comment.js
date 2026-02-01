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
    // 用户点赞的评论 id
    likeComment: [],
    // 用户点踩的评论 id
    dislikeComment: [],
  }),

  getters: {
    commentCount: (state) => state.commentList.length,
    hasMore: (state) => {
      return state.commentList.length < state.pagination.total
    },
  },

  actions: {
    // 更新用户点赞评论id列表
    updateLikeComment(lc) {
      this.likeComment = lc
    },

    // 更新用户点踩评论id列表
    updateDislikeComment(dlc) {
      this.dislikeComment = dlc
    },

    // 获取评论列表
    async fetchCommentList(videoId, params = {}) {
      this.loading = true
      try {
        const result = await axios.get(`/api/comment/${videoId}`, {
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

    // 发表评论
    async postComment(videoId, content, parentId = null) {
      try {
        const result = await axios.post(
          `/api/comment/${videoId}`,
          {
            content,
            parentId,
          },
          {
            headers: {
              Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
            },
          }
        )

        if (result.data.code === 200) {
          ElMessage.success('评论成功')
          const newComment = result.data.data
          
          // 如果是顶级评论，添加到列表开头
          if (!parentId) {
            this.commentList.unshift(newComment)
            this.pagination.total++
          }
          
          return newComment
        } else {
          ElMessage.error(result.data.message || '评论失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('评论失败:', error)
        throw error
      }
    },

    // 回复评论
    async replyComment(videoId, commentId, content) {
      return this.postComment(videoId, content, commentId)
    },

    // 点赞评论
    async likeCommentAction(commentId) {
      try {
        const result = await axios.post(
          `/api/comment/${commentId}/like`,
          {},
          {
            headers: {
              Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
            },
          }
        )

        if (result.data.code === 200) {
          // 添加到点赞列表
          if (!this.likeComment.includes(commentId)) {
            this.likeComment.push(commentId)
          }
          // 从点踩列表移除
          const dislikeIndex = this.dislikeComment.indexOf(commentId)
          if (dislikeIndex !== -1) {
            this.dislikeComment.splice(dislikeIndex, 1)
          }
          
          // 更新评论列表中的点赞数
          const comment = this.findCommentById(commentId)
          if (comment) {
            comment.likeCount = (comment.likeCount || 0) + 1
          }
          
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '点赞失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('点赞失败:', error)
        throw error
      }
    },

    // 取消点赞评论
    async unlikeCommentAction(commentId) {
      try {
        const result = await axios.delete(`/api/comment/${commentId}/like`, {
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code === 200) {
          // 从点赞列表移除
          const index = this.likeComment.indexOf(commentId)
          if (index !== -1) {
            this.likeComment.splice(index, 1)
          }
          
          // 更新评论列表中的点赞数
          const comment = this.findCommentById(commentId)
          if (comment) {
            comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1)
          }
          
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '取消点赞失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('取消点赞失败:', error)
        throw error
      }
    },

    // 点踩评论
    async dislikeCommentAction(commentId) {
      try {
        const result = await axios.post(
          `/api/comment/${commentId}/dislike`,
          {},
          {
            headers: {
              Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
            },
          }
        )

        if (result.data.code === 200) {
          // 添加到点踩列表
          if (!this.dislikeComment.includes(commentId)) {
            this.dislikeComment.push(commentId)
          }
          // 从点赞列表移除
          const likeIndex = this.likeComment.indexOf(commentId)
          if (likeIndex !== -1) {
            this.likeComment.splice(likeIndex, 1)
          }
          
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '点踩失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('点踩失败:', error)
        throw error
      }
    },

    // 删除评论
    async deleteComment(commentId) {
      try {
        const result = await axios.delete(`/api/comment/${commentId}`, {
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code === 200) {
          ElMessage.success('删除成功')
          // 从列表中移除
          this.removeCommentById(commentId)
          this.pagination.total = Math.max(0, this.pagination.total - 1)
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

    // 查找评论（包括嵌套回复）
    findCommentById(commentId) {
      for (const comment of this.commentList) {
        if (comment.id === commentId) {
          return comment
        }
        if (comment.replies && comment.replies.length > 0) {
          const found = this.findInReplies(comment.replies, commentId)
          if (found) return found
        }
      }
      return null
    },

    // 在回复中查找
    findInReplies(replies, commentId) {
      for (const reply of replies) {
        if (reply.id === commentId) {
          return reply
        }
        if (reply.replies && reply.replies.length > 0) {
          const found = this.findInReplies(reply.replies, commentId)
          if (found) return found
        }
      }
      return null
    },

    // 从列表中移除评论
    removeCommentById(commentId) {
      const index = this.commentList.findIndex((c) => c.id === commentId)
      if (index !== -1) {
        this.commentList.splice(index, 1)
        return true
      }
      
      // 在回复中查找并移除
      for (const comment of this.commentList) {
        if (comment.replies && comment.replies.length > 0) {
          if (this.removeFromReplies(comment.replies, commentId)) {
            return true
          }
        }
      }
      return false
    },

    // 从回复中移除
    removeFromReplies(replies, commentId) {
      const index = replies.findIndex((r) => r.id === commentId)
      if (index !== -1) {
        replies.splice(index, 1)
        return true
      }
      
      for (const reply of replies) {
        if (reply.replies && reply.replies.length > 0) {
          if (this.removeFromReplies(reply.replies, commentId)) {
            return true
          }
        }
      }
      return false
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
