import http from './http'
import { toCamelCase, toSnakeCase } from './fieldMapper'

/**
 * Comment API module for admin
 * Handles all comment-related API calls for admin operations
 */
export const commentApi = {
  /**
   * Get comment list for admin review
   * @param {Object} params - Query params { page, pageSize, status, keyword, videoId, userId }
   * @returns {Promise} Response with comment list
   */
  async getList(params = {}) {
    const response = await http.get('/api/admin/comment/list', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get comment detail by ID
   * @param {Number} commentId - Comment ID
   * @returns {Promise} Response with comment detail
   */
  async getDetail(commentId) {
    const response = await http.get(`/api/admin/comment/${commentId}`)
    return toCamelCase(response.data)
  },

  /**
   * Delete comment
   * @param {Number} commentId - Comment ID
   * @returns {Promise} Response
   */
  async delete(commentId) {
    const response = await http.delete(`/api/admin/comment/${commentId}`)
    return toCamelCase(response.data)
  },

  /**
   * Batch delete comments
   * @param {Array} commentIds - Array of comment IDs
   * @returns {Promise} Response
   */
  async batchDelete(commentIds) {
    const response = await http.post('/api/admin/comment/batch/delete', toSnakeCase({ commentIds }))
    return toCamelCase(response.data)
  },

  /**
   * Get reported comments
   * @param {Object} params - Query params { page, pageSize, status }
   * @returns {Promise} Response with reported comment list
   */
  async getReported(params = {}) {
    const response = await http.get('/api/admin/comment/reported', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Handle comment report
   * @param {Number} reportId - Report ID
   * @param {Object} handleData - { action, reason }
   * @returns {Promise} Response
   */
  async handleReport(reportId, handleData) {
    const response = await http.post(`/api/admin/comment/report/${reportId}/handle`, toSnakeCase(handleData))
    return toCamelCase(response.data)
  },

  /**
   * Get comment statistics
   * @param {Object} params - Query params { startDate, endDate }
   * @returns {Promise} Response with comment statistics
   */
  async getStats(params = {}) {
    const response = await http.get('/api/admin/comment/stats', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  }
}

export default commentApi
