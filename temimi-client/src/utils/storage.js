/**
 * 统一的本地存储管理工具
 */
import { STORAGE_KEYS } from '@/constants'

class StorageManager {
  /**
   * 获取 Token
   */
  getToken() {
    return localStorage.getItem(STORAGE_KEYS.TOKEN)
  }

  /**
   * 设置 Token
   */
  setToken(token) {
    if (token) {
      localStorage.setItem(STORAGE_KEYS.TOKEN, token)
    } else {
      this.removeToken()
    }
  }

  /**
   * 移除 Token
   */
  removeToken() {
    localStorage.removeItem(STORAGE_KEYS.TOKEN)
  }

  /**
   * 检查是否有 Token
   */
  hasToken() {
    return !!this.getToken()
  }

  /**
   * 获取用户信息
   */
  getUserInfo() {
    try {
      const userInfo = localStorage.getItem(STORAGE_KEYS.USER_INFO)
      return userInfo ? JSON.parse(userInfo) : null
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return null
    }
  }

  /**
   * 设置用户信息
   */
  setUserInfo(userInfo) {
    try {
      localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(userInfo))
    } catch (error) {
      console.error('保存用户信息失败:', error)
    }
  }

  /**
   * 移除用户信息
   */
  removeUserInfo() {
    localStorage.removeItem(STORAGE_KEYS.USER_INFO)
  }

  /**
   * 获取搜索历史
   */
  getSearchHistory() {
    try {
      const history = localStorage.getItem(STORAGE_KEYS.SEARCH_HISTORY)
      return history ? JSON.parse(history) : []
    } catch (error) {
      console.error('获取搜索历史失败:', error)
      return []
    }
  }

  /**
   * 设置搜索历史
   */
  setSearchHistory(history) {
    try {
      localStorage.setItem(STORAGE_KEYS.SEARCH_HISTORY, JSON.stringify(history))
    } catch (error) {
      console.error('保存搜索历史失败:', error)
    }
  }

  /**
   * 清空搜索历史
   */
  clearSearchHistory() {
    localStorage.removeItem(STORAGE_KEYS.SEARCH_HISTORY)
  }

  /**
   * 清空所有存储
   */
  clearAll() {
    this.removeToken()
    this.removeUserInfo()
    this.clearSearchHistory()
  }
}

// 导出单例
export const storage = new StorageManager()
export default storage
