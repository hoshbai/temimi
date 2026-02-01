import http from './http'
import { toCamelCase, toSnakeCase } from './fieldMapper'

/**
 * Danmu (Bullet Comment) API module for admin
 * Handles all danmu-related API calls for admin operations
 */
export const danmuApi = {
  /**
   * Get danmu list for admin review
   * @param {Object} params - Query params { page, pageSize, videoId, userId, keyword }
   * @returns {Promise} Response with danmu list
   */
  async getList(params = {}) {
    const response = await http.get('/api/admin/danmu/list', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get danmu detail by ID
   * @param {Number} danmuId - Danmu ID
   * @returns {Promise} Response with danmu detail
   */
  async getDetail(danmuId) {
    const response = await http.get(`/api/admin/danmu/${danmuId}`)
    return toCamelCase(response.data)
  },

  /**
   * Delete danmu
   * @param {Number} danmuId - Danmu ID
   * @returns {Promise} Response
   */
  async delete(danmuId) {
    const response = await http.delete(`/api/admin/danmu/${danmuId}`)
    return toCamelCase(response.data)
  },

  /**
   * Batch delete danmu
   * @param {Array} danmuIds - Array of danmu IDs
   * @returns {Promise} Response
   */
  async batchDelete(danmuIds) {
    const response = await http.post('/api/admin/danmu/batch/delete', toSnakeCase({ danmuIds }))
    return toCamelCase(response.data)
  },

  /**
   * Get reported danmu
   * @param {Object} params - Query params { page, pageSize, status }
   * @returns {Promise} Response with reported danmu list
   */
  async getReported(params = {}) {
    const response = await http.get('/api/admin/danmu/reported', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Handle danmu report
   * @param {Number} reportId - Report ID
   * @param {Object} handleData - { action, reason }
   * @returns {Promise} Response
   */
  async handleReport(reportId, handleData) {
    const response = await http.post(`/api/admin/danmu/report/${reportId}/handle`, toSnakeCase(handleData))
    return toCamelCase(response.data)
  },

  /**
   * Get danmu statistics
   * @param {Object} params - Query params { startDate, endDate, videoId }
   * @returns {Promise} Response with danmu statistics
   */
  async getStats(params = {}) {
    const response = await http.get('/api/admin/danmu/stats', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get danmu list for a specific video
   * @param {Number} videoId - Video ID
   * @param {Object} params - Query params { page, pageSize }
   * @returns {Promise} Response with danmu list
   */
  async getVideoDanmu(videoId, params = {}) {
    const response = await http.get(`/api/admin/danmu/video/${videoId}`, {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  }
}

export default danmuApi
