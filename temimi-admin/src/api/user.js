import http from './http'
import { toCamelCase, toSnakeCase } from './fieldMapper'

/**
 * User API module for admin
 * Handles all user-related API calls for admin operations
 */
export const userApi = {
  /**
   * Admin login with credentials
   * @param {Object} credentials - { username, password }
   * @returns {Promise} Response with token and admin user data
   */
  async login(credentials) {
    const response = await http.post('/api/admin/account/login', toSnakeCase(credentials))
    return toCamelCase(response.data)
  },

  /**
   * Admin logout
   * @returns {Promise} Response
   */
  async logout() {
    const response = await http.get('/api/admin/account/logout')
    return toCamelCase(response.data)
  },

  /**
   * Get current admin's personal info
   * @returns {Promise} Response with admin user data
   */
  async getPersonalInfo() {
    const response = await http.get('/api/admin/personal/info')
    return toCamelCase(response.data)
  },

  /**
   * Get user list (for admin management)
   * @param {Object} params - Query params { page, pageSize, keyword, status }
   * @returns {Promise} Response with user list
   */
  async getUserList(params = {}) {
    const response = await http.get('/api/admin/user/list', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get user detail by ID
   * @param {Number} userId - User ID
   * @returns {Promise} Response with user detail
   */
  async getUserDetail(userId) {
    const response = await http.get(`/api/admin/user/${userId}`)
    return toCamelCase(response.data)
  },

  /**
   * Update user status (ban/unban)
   * @param {Number} userId - User ID
   * @param {Object} data - { status, reason }
   * @returns {Promise} Response
   */
  async updateUserStatus(userId, data) {
    const response = await http.put(`/api/admin/user/${userId}/status`, toSnakeCase(data))
    return toCamelCase(response.data)
  },

  /**
   * Delete user
   * @param {Number} userId - User ID
   * @returns {Promise} Response
   */
  async deleteUser(userId) {
    const response = await http.delete(`/api/admin/user/${userId}`)
    return toCamelCase(response.data)
  },

  /**
   * Get user statistics
   * @param {Number} userId - User ID
   * @returns {Promise} Response with user statistics
   */
  async getUserStats(userId) {
    const response = await http.get(`/api/admin/user/${userId}/stats`)
    return toCamelCase(response.data)
  }
}

export default userApi
