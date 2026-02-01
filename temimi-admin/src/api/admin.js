import http from './http'
import { toCamelCase, toSnakeCase } from './fieldMapper'

/**
 * Admin API module
 * Handles admin-specific operations like statistics, system management, etc.
 */
export const adminApi = {
  /**
   * Get dashboard statistics
   * @param {Object} params - Query params { startDate, endDate }
   * @returns {Promise} Response with dashboard statistics
   */
  async getDashboardStats(params = {}) {
    const response = await http.get('/api/admin/dashboard/stats', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get overall platform statistics
   * @returns {Promise} Response with platform statistics
   */
  async getPlatformStats() {
    const response = await http.get('/api/admin/stats/platform')
    return toCamelCase(response.data)
  },

  /**
   * Get user statistics
   * @param {Object} params - Query params { startDate, endDate, groupBy }
   * @returns {Promise} Response with user statistics
   */
  async getUserStats(params = {}) {
    const response = await http.get('/api/admin/stats/user', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get video statistics
   * @param {Object} params - Query params { startDate, endDate, groupBy }
   * @returns {Promise} Response with video statistics
   */
  async getVideoStats(params = {}) {
    const response = await http.get('/api/admin/stats/video', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get content statistics (comments, danmu, etc.)
   * @param {Object} params - Query params { startDate, endDate, type }
   * @returns {Promise} Response with content statistics
   */
  async getContentStats(params = {}) {
    const response = await http.get('/api/admin/stats/content', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get system logs
   * @param {Object} params - Query params { page, pageSize, level, startDate, endDate }
   * @returns {Promise} Response with system logs
   */
  async getSystemLogs(params = {}) {
    const response = await http.get('/api/admin/system/logs', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get operation logs
   * @param {Object} params - Query params { page, pageSize, adminId, action, startDate, endDate }
   * @returns {Promise} Response with operation logs
   */
  async getOperationLogs(params = {}) {
    const response = await http.get('/api/admin/operation/logs', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Get admin list
   * @param {Object} params - Query params { page, pageSize, keyword, role }
   * @returns {Promise} Response with admin list
   */
  async getAdminList(params = {}) {
    const response = await http.get('/api/admin/list', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Create new admin
   * @param {Object} adminData - { username, password, email, role }
   * @returns {Promise} Response with created admin
   */
  async createAdmin(adminData) {
    const response = await http.post('/api/admin/create', toSnakeCase(adminData))
    return toCamelCase(response.data)
  },

  /**
   * Update admin info
   * @param {Number} adminId - Admin ID
   * @param {Object} adminData - Updated admin data
   * @returns {Promise} Response
   */
  async updateAdmin(adminId, adminData) {
    const response = await http.put(`/api/admin/${adminId}`, toSnakeCase(adminData))
    return toCamelCase(response.data)
  },

  /**
   * Delete admin
   * @param {Number} adminId - Admin ID
   * @returns {Promise} Response
   */
  async deleteAdmin(adminId) {
    const response = await http.delete(`/api/admin/${adminId}`)
    return toCamelCase(response.data)
  },

  /**
   * Get system configuration
   * @returns {Promise} Response with system configuration
   */
  async getSystemConfig() {
    const response = await http.get('/api/admin/system/config')
    return toCamelCase(response.data)
  },

  /**
   * Update system configuration
   * @param {Object} configData - Configuration data
   * @returns {Promise} Response
   */
  async updateSystemConfig(configData) {
    const response = await http.put('/api/admin/system/config', toSnakeCase(configData))
    return toCamelCase(response.data)
  },

  /**
   * Get pending review count (videos, comments, reports)
   * @returns {Promise} Response with pending counts
   */
  async getPendingReviewCount() {
    const response = await http.get('/api/admin/review/pending/count')
    return toCamelCase(response.data)
  },

  /**
   * Get report list
   * @param {Object} params - Query params { page, pageSize, type, status }
   * @returns {Promise} Response with report list
   */
  async getReportList(params = {}) {
    const response = await http.get('/api/admin/report/list', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Handle report
   * @param {Number} reportId - Report ID
   * @param {Object} handleData - { action, reason }
   * @returns {Promise} Response
   */
  async handleReport(reportId, handleData) {
    const response = await http.post(`/api/admin/report/${reportId}/handle`, toSnakeCase(handleData))
    return toCamelCase(response.data)
  },

  /**
   * Get announcement list
   * @param {Object} params - Query params { page, pageSize, status }
   * @returns {Promise} Response with announcement list
   */
  async getAnnouncementList(params = {}) {
    const response = await http.get('/api/admin/announcement/list', {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Create announcement
   * @param {Object} announcementData - { title, content, type, priority }
   * @returns {Promise} Response with created announcement
   */
  async createAnnouncement(announcementData) {
    const response = await http.post('/api/admin/announcement', toSnakeCase(announcementData))
    return toCamelCase(response.data)
  },

  /**
   * Update announcement
   * @param {Number} announcementId - Announcement ID
   * @param {Object} announcementData - Updated announcement data
   * @returns {Promise} Response
   */
  async updateAnnouncement(announcementId, announcementData) {
    const response = await http.put(`/api/admin/announcement/${announcementId}`, toSnakeCase(announcementData))
    return toCamelCase(response.data)
  },

  /**
   * Delete announcement
   * @param {Number} announcementId - Announcement ID
   * @returns {Promise} Response
   */
  async deleteAnnouncement(announcementId) {
    const response = await http.delete(`/api/admin/announcement/${announcementId}`)
    return toCamelCase(response.data)
  },

  /**
   * Export data
   * @param {Object} exportParams - { type, startDate, endDate, format }
   * @returns {Promise} Response with export file URL or data
   */
  async exportData(exportParams) {
    const response = await http.post('/api/admin/export', toSnakeCase(exportParams), {
      responseType: 'blob'
    })
    return response.data
  },

  /**
   * Get category list
   * @returns {Promise} Response with category list
   */
  async getCategoryList() {
    const response = await http.get('/api/admin/category/list')
    return toCamelCase(response.data)
  },

  /**
   * Create category
   * @param {Object} categoryData - { name, description, icon }
   * @returns {Promise} Response with created category
   */
  async createCategory(categoryData) {
    const response = await http.post('/api/admin/category', toSnakeCase(categoryData))
    return toCamelCase(response.data)
  },

  /**
   * Update category
   * @param {Number} categoryId - Category ID
   * @param {Object} categoryData - Updated category data
   * @returns {Promise} Response
   */
  async updateCategory(categoryId, categoryData) {
    const response = await http.put(`/api/admin/category/${categoryId}`, toSnakeCase(categoryData))
    return toCamelCase(response.data)
  },

  /**
   * Delete category
   * @param {Number} categoryId - Category ID
   * @returns {Promise} Response
   */
  async deleteCategory(categoryId) {
    const response = await http.delete(`/api/admin/category/${categoryId}`)
    return toCamelCase(response.data)
  }
}

export default adminApi
