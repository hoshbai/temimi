import http from './http'
import { toCamelCase, toSnakeCase } from './fieldMapper'

/**
 * Video API module for admin
 * Handles all video-related API calls for admin operations
 */
export const videoApi = {
  /**
   * Get video list for admin review
   * @param {Object} params - Query params { page, pageSize, status, keyword, userId }
   * @returns {Promise} Response with video list
   */
  async getList(params = {}) {
    const response = await http.get('/api/admin/video/list', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get video detail by ID
   * @param {Number} videoId - Video ID
   * @returns {Promise} Response with video detail
   */
  async getDetail(videoId) {
    const response = await http.get(`/api/admin/video/${videoId}`)
    return toCamelCase(response.data)
  },

  /**
   * Review video (approve/reject)
   * @param {Number} videoId - Video ID
   * @param {Object} reviewData - { status, reason }
   * @returns {Promise} Response
   */
  async review(videoId, reviewData) {
    const response = await http.post(`/api/admin/video/${videoId}/review`, toSnakeCase(reviewData))
    return toCamelCase(response.data)
  },

  /**
   * Approve video
   * @param {Number} videoId - Video ID
   * @returns {Promise} Response
   */
  async approve(videoId) {
    const response = await http.post(`/api/admin/video/${videoId}/approve`)
    return toCamelCase(response.data)
  },

  /**
   * Reject video
   * @param {Number} videoId - Video ID
   * @param {String} reason - Rejection reason
   * @returns {Promise} Response
   */
  async reject(videoId, reason) {
    const response = await http.post(`/api/admin/video/${videoId}/reject`, toSnakeCase({ reason }))
    return toCamelCase(response.data)
  },

  /**
   * Delete video
   * @param {Number} videoId - Video ID
   * @returns {Promise} Response
   */
  async delete(videoId) {
    const response = await http.delete(`/api/admin/video/${videoId}`)
    return toCamelCase(response.data)
  },

  /**
   * Get video statistics
   * @param {Number} videoId - Video ID
   * @returns {Promise} Response with video statistics
   */
  async getStats(videoId) {
    const response = await http.get(`/api/admin/video/${videoId}/stats`)
    return toCamelCase(response.data)
  },

  /**
   * Get pending review videos count
   * @returns {Promise} Response with count
   */
  async getPendingCount() {
    const response = await http.get('/api/admin/video/pending/count')
    return toCamelCase(response.data)
  },

  /**
   * Batch approve videos
   * @param {Array} videoIds - Array of video IDs
   * @returns {Promise} Response
   */
  async batchApprove(videoIds) {
    const response = await http.post('/api/admin/video/batch/approve', toSnakeCase({ videoIds }))
    return toCamelCase(response.data)
  },

  /**
   * Batch reject videos
   * @param {Array} videoIds - Array of video IDs
   * @param {String} reason - Rejection reason
   * @returns {Promise} Response
   */
  async batchReject(videoIds, reason) {
    const response = await http.post('/api/admin/video/batch/reject', toSnakeCase({ videoIds, reason }))
    return toCamelCase(response.data)
  },

  /**
   * Batch delete videos
   * @param {Array} videoIds - Array of video IDs
   * @returns {Promise} Response
   */
  async batchDelete(videoIds) {
    const response = await http.post('/api/admin/video/batch/delete', toSnakeCase({ videoIds }))
    return toCamelCase(response.data)
  },

  /**
   * Update video info
   * @param {Number} videoId - Video ID
   * @param {Object} videoData - Updated video data
   * @returns {Promise} Response
   */
  async update(videoId, videoData) {
    const response = await http.put(`/api/admin/video/${videoId}`, toSnakeCase(videoData))
    return toCamelCase(response.data)
  }
}

export default videoApi
