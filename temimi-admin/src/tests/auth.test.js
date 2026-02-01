import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'
import axios from 'axios'

// Mock axios
vi.mock('axios')

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn()
  }
}))

// Mock router
vi.mock('../router', () => ({
  default: {
    push: vi.fn()
  }
}))

describe('Admin Authentication Flow Tests', () => {
  let userStore

  beforeEach(() => {
    // Create a fresh pinia instance for each test
    setActivePinia(createPinia())
    userStore = useUserStore()
    
    // Clear localStorage
    localStorage.clear()
    
    // Reset all mocks
    vi.clearAllMocks()
  })

  describe('Admin login with valid credentials', () => {
    it('should successfully login admin and store token', async () => {
      const mockLoginResponse = {
        data: {
          code: 200,
          message: 'success',
          data: {
            token: 'admin-jwt-token-12345'
          }
        }
      }

      const mockUserInfoResponse = {
        data: {
          code: 200,
          message: 'success',
          data: {
            uid: 1,
            username: 'admin',
            email: 'admin@example.com',
            avatar: 'https://example.com/admin-avatar.jpg',
            role: 'admin'
          }
        }
      }

      // Mock axios responses
      axios.post.mockResolvedValueOnce(mockLoginResponse)
      axios.get.mockResolvedValueOnce(mockUserInfoResponse)

      const credentials = {
        username: 'admin',
        password: 'adminpass123'
      }

      const result = await userStore.login(credentials)

      // Verify login was called with correct credentials
      expect(axios.post).toHaveBeenCalledWith('/api/admin/account/login', credentials)
      
      // Verify token was stored
      expect(localStorage.getItem('teri_token')).toBe('admin-jwt-token-12345')
      
      // Verify user info was fetched
      expect(axios.get).toHaveBeenCalledWith('/api/admin/personal/info', {
        headers: {
          Authorization: 'Bearer admin-jwt-token-12345'
        }
      })
      
      // Verify store state was updated
      expect(userStore.isLogin).toBe(true)
      expect(userStore.user.uid).toBe(1)
      expect(userStore.user.username).toBe('admin')
      expect(result.code).toBe(200)
    })
  })

  describe('Admin login with invalid credentials', () => {
    it('should fail login and show error message', async () => {
      const mockErrorResponse = {
        data: {
          code: 401,
          message: '管理员用户名或密码错误',
          data: null
        }
      }

      axios.post.mockResolvedValueOnce(mockErrorResponse)

      const credentials = {
        username: 'wrongadmin',
        password: 'wrongpassword'
      }

      await expect(userStore.login(credentials)).rejects.toThrow()
      
      // Verify token was not stored
      expect(localStorage.getItem('teri_token')).toBeNull()
      
      // Verify user is not logged in
      expect(userStore.isLogin).toBe(false)
    })

    it('should handle network errors during admin login', async () => {
      axios.post.mockRejectedValueOnce(new Error('Network error'))

      const credentials = {
        username: 'admin',
        password: 'password123'
      }

      await expect(userStore.login(credentials)).rejects.toThrow('Network error')
      
      // Verify token was not stored
      expect(localStorage.getItem('teri_token')).toBeNull()
      
      // Verify user is not logged in
      expect(userStore.isLogin).toBe(false)
    })
  })

  describe('Admin logout', () => {
    it('should successfully logout and clear admin data', async () => {
      // Setup: Login first
      userStore.isLogin = true
      userStore.user = {
        uid: 1,
        username: 'admin'
      }
      localStorage.setItem('teri_token', 'admin-token')

      axios.get.mockResolvedValueOnce({ data: { code: 200 } })

      // Logout
      userStore.logout()

      // Verify token was removed
      expect(localStorage.getItem('teri_token')).toBeNull()
      
      // Verify store state was cleared
      expect(userStore.isLogin).toBe(false)
      expect(userStore.user).toEqual({})
    })

    it('should clear data even if logout API fails', async () => {
      // Setup: Login first
      userStore.isLogin = true
      userStore.user = {
        uid: 1,
        username: 'admin'
      }
      localStorage.setItem('teri_token', 'admin-token')

      axios.get.mockRejectedValueOnce(new Error('Network error'))

      // Logout
      userStore.logout()

      // Verify token was still removed
      expect(localStorage.getItem('teri_token')).toBeNull()
      
      // Verify store state was still cleared
      expect(userStore.isLogin).toBe(false)
      expect(userStore.user).toEqual({})
    })
  })

  describe('Admin token persistence', () => {
    it('should retrieve token from localStorage on store initialization', () => {
      localStorage.setItem('teri_token', 'persisted-admin-token')
      
      const newStore = useUserStore()
      
      expect(newStore.token).toBe('persisted-admin-token')
    })

    it('should return null when no token exists', () => {
      expect(userStore.token).toBeNull()
    })

    it('should maintain token across page refreshes', async () => {
      // Simulate login
      const mockLoginResponse = {
        data: {
          code: 200,
          data: {
            token: 'refresh-admin-token'
          }
        }
      }

      const mockUserInfoResponse = {
        data: {
          code: 200,
          data: {
            uid: 1,
            username: 'admin'
          }
        }
      }

      axios.post.mockResolvedValueOnce(mockLoginResponse)
      axios.get.mockResolvedValueOnce(mockUserInfoResponse)

      await userStore.login({ username: 'admin', password: 'pass' })

      // Verify token is stored
      expect(localStorage.getItem('teri_token')).toBe('refresh-admin-token')

      // Simulate page refresh by creating new store instance
      const refreshedStore = useUserStore()
      
      // Token should still be available
      expect(refreshedStore.token).toBe('refresh-admin-token')
    })
  })

  describe('Admin user info retrieval', () => {
    it('should fetch and update admin user info when logged in', async () => {
      localStorage.setItem('teri_token', 'valid-admin-token')

      const mockUserInfoResponse = {
        data: {
          code: 200,
          data: {
            uid: 1,
            username: 'admin',
            email: 'admin@example.com',
            avatar: 'https://example.com/admin-avatar.jpg',
            role: 'admin'
          }
        }
      }

      axios.get.mockResolvedValueOnce(mockUserInfoResponse)

      await userStore.getPersonalInfo()

      expect(userStore.isLogin).toBe(true)
      expect(userStore.user.uid).toBe(1)
      expect(userStore.user.username).toBe('admin')
      expect(userStore.user.email).toBe('admin@example.com')
    })
  })

  describe('Admin store getters', () => {
    it('should return correct admin user properties from getters', () => {
      userStore.user = {
        uid: 1,
        username: 'admin',
        avatar: 'https://example.com/admin-avatar.jpg'
      }

      expect(userStore.userId).toBe(1)
      expect(userStore.username).toBe('admin')
      expect(userStore.avatar).toBe('https://example.com/admin-avatar.jpg')
    })

    it('should handle undefined user properties gracefully', () => {
      userStore.user = {}

      expect(userStore.userId).toBeUndefined()
      expect(userStore.username).toBeUndefined()
      expect(userStore.avatar).toBeUndefined()
    })
  })
})
