import http from './http'
import { toCamelCase, toSnakeCase } from './fieldMapper'

/**
 * Danmu (Bullet Comment) API module
 * Handles all danmu-related API calls and WebSocket connections
 */
export const danmuApi = {
  /**
   * Get danmu list for a video
   * @param {Number} videoId - Video ID
   * @param {Object} params - Query params { startTime, endTime }
   * @returns {Promise} Response with danmu list
   */
  async getList(videoId, params = {}) {
    const response = await http.get(`/api/danmu/video/${videoId}`, {
      params: toSnakeCase(params)
    })
    return toCamelCase(response.data)
  },

  /**
   * Post a new danmu
   * @param {Object} danmuData - { videoId, content, time, color, type }
   * @returns {Promise} Response with created danmu
   */
  async post(danmuData) {
    const response = await http.post('/api/danmu', toSnakeCase(danmuData))
    return toCamelCase(response.data)
  },

  /**
   * Delete danmu
   * @param {Number} danmuId - Danmu ID
   * @returns {Promise} Response
   */
  async delete(danmuId) {
    const response = await http.delete(`/api/danmu/${danmuId}`)
    return toCamelCase(response.data)
  },

  /**
   * Report a danmu
   * @param {Number} danmuId - Danmu ID
   * @param {Object} reportData - { reason, description }
   * @returns {Promise} Response
   */
  async report(danmuId, reportData) {
    const response = await http.post(`/api/danmu/${danmuId}/report`, toSnakeCase(reportData))
    return toCamelCase(response.data)
  },

  /**
   * Get danmu statistics for a video
   * @param {Number} videoId - Video ID
   * @returns {Promise} Response with danmu statistics
   */
  async getStats(videoId) {
    const response = await http.get(`/api/danmu/video/${videoId}/stats`)
    return toCamelCase(response.data)
  },

  /**
   * Create WebSocket connection for real-time danmu
   * @param {Number} videoId - Video ID
   * @param {Object} callbacks - { onMessage, onError, onClose, onOpen }
   * @returns {WebSocket} WebSocket instance
   */
  createWebSocket(videoId, callbacks = {}) {
    const wsBaseUrl = import.meta.env.VITE_WS_DANMU_URL || 'ws://localhost:8080'
    const token = localStorage.getItem('teri_token')
    const wsUrl = `${wsBaseUrl}/danmu/${videoId}?token=${encodeURIComponent(token)}`
    
    const ws = new WebSocket(wsUrl)

    ws.addEventListener('open', (event) => {
      console.log('Danmu WebSocket connected')
      if (callbacks.onOpen) {
        callbacks.onOpen(event)
      }
    })

    ws.addEventListener('message', (event) => {
      try {
        const data = JSON.parse(event.data)
        const camelData = toCamelCase(data)
        if (callbacks.onMessage) {
          callbacks.onMessage(camelData)
        }
      } catch (error) {
        console.error('Failed to parse danmu message:', error)
      }
    })

    ws.addEventListener('error', (event) => {
      console.error('Danmu WebSocket error:', event)
      if (callbacks.onError) {
        callbacks.onError(event)
      }
    })

    ws.addEventListener('close', (event) => {
      console.log('Danmu WebSocket closed')
      if (callbacks.onClose) {
        callbacks.onClose(event)
      }
    })

    return ws
  },

  /**
   * Send danmu through WebSocket
   * @param {WebSocket} ws - WebSocket instance
   * @param {Object} danmuData - { content, time, color, type }
   */
  sendWebSocket(ws, danmuData) {
    if (ws && ws.readyState === WebSocket.OPEN) {
      const snakeData = toSnakeCase(danmuData)
      ws.send(JSON.stringify(snakeData))
    } else {
      console.error('WebSocket is not connected')
    }
  },

  /**
   * Close WebSocket connection
   * @param {WebSocket} ws - WebSocket instance
   */
  closeWebSocket(ws) {
    if (ws) {
      ws.close()
    }
  }
}

export default danmuApi
