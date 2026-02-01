import http from './http'
import { toCamelCase, toSnakeCase } from './fieldMapper'

/**
 * Video API module
 * Handles all video-related API calls
 */
export const videoApi = {
  /**
   * Get video list
   * @param {Object} params - Query params { page, pageSize, category, sort, keyword }
   * @returns {Promise} Response with video list
   */
  async getList(params = {}) {
    const response = await http.get('/api/video/list', {
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
    const response = await http.get(`/api/video/${videoId}`)
    return toCamelCase(response.data)
  },

  /**
   * Upload video
   * @param {FormData} formData - Video upload form data
   * @param {Function} onProgress - Upload progress callback
   * @returns {Promise} Response with uploaded video info
   */
  async upload(formData, onProgress) {
    const response = await http.post('/api/video/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(percentCompleted)
        }
      }
    })
    return toCamelCase(response.data)
  },

  /**
   * Update video info
   * @param {Number} videoId - Video ID
   * @param {Object} videoData - Updated video data
   * @returns {Promise} Response
   */
  async update(videoId, videoData) {
    const response = await http.put(`/api/video/${videoId}`, toSnakeCase(videoData))
    return toCamelCase(response.data)
  },

  /**
   * Delete video
   * @param {Number} videoId - Video ID
   * @returns {Promise} Response
   */
  async delete(videoId) {
    const response = await http.delete(`/api/video/${videoId}`)
    return toCamelCase(response.data)
  },

  /**
   * Get video statistics
   * @param {Number} videoId - Video ID
   * @returns {Promise} Response with video statistics
   */
  async getStats(videoId) {
    const response = await http.get(`/api/video/${videoId}/stats`)
    return toCamelCase(response.data)
  },

  /**
   * Like video
   * @param {Number} videoId - Video ID
   * @returns {Promise} Response
   */
  async like(videoId) {
    const response = await http.post(`/api/video/interaction/like/${videoId}`)
    return toCamelCase(response.data)
  },

  /**
   * Unlike video
   * @param {Number} videoId - Video ID
   * @returns {Promise} Response
   */
  async unlike(videoId) {
    const response = await http.post(`/api/video/interaction/unlike/${videoId}`)
    return toCamelCase(response.data)
  },

  /**
   * Favorite video
   * @param {Number} videoId - Video ID
   * @param {Number} folderId - Favorite folder ID (optional)
   * @returns {Promise} Response
   */
  async favorite(videoId, folderId) {
    const response = await http.post(`/api/video/${videoId}/favorite`, toSnakeCase({ folderId }))
    return toCamelCase(response.data)
  },

  /**
   * Unfavorite video
   * @param {Number} videoId - Video ID
   * @returns {Promise} Response
   */
  async unfavorite(videoId) {
    const response = await http.delete(`/api/video/${videoId}/favorite`)
    return toCamelCase(response.data)
  },

  /**
   * Coin video (投币)
   * @param {Number} videoId - Video ID
   * @param {Number} count - Number of coins (1 or 2)
   * @returns {Promise} Response
   */
  async coin(videoId, count = 1) {
    const response = await http.post(`/api/video/${videoId}/coin`, toSnakeCase({ count }))
    return toCamelCase(response.data)
  },

  /**
   * Share video
   * @param {Number} videoId - Video ID
   * @returns {Promise} Response
   */
  async share(videoId) {
    const response = await http.post(`/api/video/${videoId}/share`)
    return toCamelCase(response.data)
  },

  /**
   * Get recommended videos
   * @param {Object} params - Query params { page, pageSize }
   * @returns {Promise} Response with recommended video list
   */
  async getRecommended(params = {}) {
    const response = await http.get('/api/video/recommended', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get trending videos
   * @param {Object} params - Query params { page, pageSize }
   * @returns {Promise} Response with trending video list
   */
  async getTrending(params = {}) {
    const response = await http.get('/api/video/trending', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Search videos
   * @param {Object} params - Query params { keyword, page, pageSize, sort }
   * @returns {Promise} Response with search results
   */
  async search(params = {}) {
    const response = await http.get('/api/video/search', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Increment video view count
   * @param {Number} videoId - Video ID
   * @returns {Promise} Response
   */
  async incrementView(videoId) {
    const response = await http.post(`/api/video/${videoId}/view`)
    return toCamelCase(response.data)
  }
}

export default videoApi
