import http from './http'
import { toCamelCase, toSnakeCase } from './fieldMapper'

/**
 * Comment API module
 * Handles all comment-related API calls
 */
export const commentApi = {
  /**
   * Get comment list for a video
   * @param {Number} videoId - Video ID
   * @param {Object} params - Query params { page, pageSize, sort }
   * @returns {Promise} Response with comment list
   */
  async getList(videoId, params = {}) {
    const response = await http.get('/api/comment/get', {
      params: { vid: videoId, offset: 0, type: 1 }
    })
    // 后端返回 { comments: [...], more: boolean }
    const result = toCamelCase(response.data)
    return { data: { list: result.data?.comments || [] } }
  },

  /**
   * Get comment detail by ID
   * @param {Number} commentId - Comment ID
   * @returns {Promise} Response with comment detail
   */
  async getDetail(commentId) {
    const response = await http.get(`/api/comment/${commentId}`)
    return toCamelCase(response.data)
  },

  /**
   * Post a new comment
   * @param {Object} commentData - { videoId, content, parentId }
   * @returns {Promise} Response with created comment
   */
  async post(commentData) {
    // 后端使用 form params: vid, root_id, parent_id, to_user_id, content
    const params = new URLSearchParams()
    params.append('vid', commentData.videoId)
    params.append('root_id', commentData.parentId || 0)
    params.append('parent_id', commentData.parentId || 0)
    params.append('to_user_id', commentData.toUserId || 0)
    params.append('content', commentData.content)
    const response = await http.post('/api/comment/add', params)
    return toCamelCase(response.data)
  },

  /**
   * Reply to a comment
   * @param {Number} commentId - Parent comment ID
   * @param {Object} replyData - { content, replyToUserId }
   * @returns {Promise} Response with created reply
   */
  async reply(commentId, replyData) {
    const response = await http.post(`/api/comment/${commentId}/reply`, toSnakeCase(replyData))
    return toCamelCase(response.data)
  },

  /**
   * Update comment
   * @param {Number} commentId - Comment ID
   * @param {Object} commentData - { content }
   * @returns {Promise} Response
   */
  async update(commentId, commentData) {
    const response = await http.put(`/api/comment/${commentId}`, toSnakeCase(commentData))
    return toCamelCase(response.data)
  },

  /**
   * Delete comment
   * @param {Number} commentId - Comment ID
   * @returns {Promise} Response
   */
  async delete(commentId) {
    const response = await http.delete(`/api/comment/${commentId}`)
    return toCamelCase(response.data)
  },

  /**
   * Like a comment
   * @param {Number} commentId - Comment ID
   * @returns {Promise} Response
   */
  async like(commentId) {
    const response = await http.post(`/api/comment/${commentId}/like`)
    return toCamelCase(response.data)
  },

  /**
   * Unlike a comment
   * @param {Number} commentId - Comment ID
   * @returns {Promise} Response
   */
  async unlike(commentId) {
    const response = await http.delete(`/api/comment/${commentId}/like`)
    return toCamelCase(response.data)
  },

  /**
   * Get replies for a comment
   * @param {Number} commentId - Comment ID
   * @param {Object} params - Query params { page, pageSize }
   * @returns {Promise} Response with reply list
   */
  async getReplies(commentId, params = {}) {
    const response = await http.get(`/api/comment/${commentId}/replies`, {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get user's comments
   * @param {Number} userId - User ID
   * @param {Object} params - Query params { page, pageSize }
   * @returns {Promise} Response with comment list
   */
  async getUserComments(userId, params = {}) {
    const response = await http.get(`/api/comment/user/${userId}`, {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Report a comment
   * @param {Number} commentId - Comment ID
   * @param {Object} reportData - { reason, description }
   * @returns {Promise} Response
   */
  async report(commentId, reportData) {
    const response = await http.post(`/api/comment/${commentId}/report`, toSnakeCase(reportData))
    return toCamelCase(response.data)
  }
}

export default commentApi
