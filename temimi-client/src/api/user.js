import http from './http'
import { toCamelCase, toSnakeCase } from './fieldMapper'

/**
 * User API module
 * Handles all user-related API calls
 */
export const userApi = {
  /**
   * Login with credentials
   * @param {Object} credentials - { username, password }
   * @returns {Promise} Response with token and user data
   */
  async login(credentials) {
    const response = await http.post('/api/user/account/login', toSnakeCase(credentials))
    return toCamelCase(response.data)
  },

  /**
   * Register new user account
   * @param {Object} userData - User registration data
   * @returns {Promise} Response with registration result
   */
  async register(userData) {
    const response = await http.post('/api/user/account/register', toSnakeCase(userData))
    return toCamelCase(response.data)
  },

  /**
   * Logout current user
   * @returns {Promise} Response
   */
  async logout() {
    const response = await http.get('/api/user/account/logout')
    return toCamelCase(response.data)
  },

  /**
   * Get current user's personal info
   * @returns {Promise} Response with user data
   */
  async getPersonalInfo() {
    const response = await http.get('/api/user/personal/info')
    return toCamelCase(response.data)
  },

  /**
   * Get user profile by user ID
   * @param {Number} userId - User ID
   * @returns {Promise} Response with user profile data
   */
  async getProfile(userId) {
    const response = await http.get(`/api/user/${userId}`)
    return toCamelCase(response.data)
  },

  /**
   * Update user profile
   * @param {Object} userData - Updated user data
   * @returns {Promise} Response with updated user data
   */
  async updateProfile(userData) {
    const response = await http.put('/api/user/profile', toSnakeCase(userData))
    return toCamelCase(response.data)
  },

  /**
   * Follow a user
   * @param {Number} userId - User ID to follow
   * @returns {Promise} Response
   */
  async followUser(userId) {
    const response = await http.post(`/api/user/follow/${userId}`)
    return toCamelCase(response.data)
  },

  /**
   * Unfollow a user
   * @param {Number} userId - User ID to unfollow
   * @returns {Promise} Response
   */
  async unfollowUser(userId) {
    const response = await http.delete(`/api/user/follow/${userId}`)
    return toCamelCase(response.data)
  },

  /**
   * Get user's followers list
   * @param {Number} userId - User ID
   * @param {Object} params - Pagination params { page, pageSize }
   * @returns {Promise} Response with followers list
   */
  async getFollowers(userId, params = {}) {
    const response = await http.get(`/api/user/${userId}/followers`, {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get user's following list
   * @param {Number} userId - User ID
   * @param {Object} params - Pagination params { page, pageSize }
   * @returns {Promise} Response with following list
   */
  async getFollowing(userId, params = {}) {
    const response = await http.get(`/api/user/${userId}/following`, {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get user's uploaded videos
   * @param {Number} userId - User ID
   * @param {Object} params - Pagination params { page, pageSize }
   * @returns {Promise} Response with video list
   */
  async getUserVideos(userId, params = {}) {
    const response = await http.get(`/api/user/${userId}/videos`, {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get user statistics
   * @param {Number} userId - User ID
   * @returns {Promise} Response with user statistics
   */
  async getUserStats(userId) {
    const response = await http.get(`/api/user/${userId}/stats`)
    return toCamelCase(response.data)
  }
}

export default userApi
