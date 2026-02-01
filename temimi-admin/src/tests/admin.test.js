import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAdminStore } from '@/stores/admin'
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

describe('Admin Features Tests', () => {
  let adminStore

  beforeEach(() => {
    // Create a fresh pinia instance for each test
    setActivePinia(createPinia())
    adminStore = useAdminStore()
    
    // Clear localStorage
    localStorage.clear()
    
    // Reset all mocks
    vi.clearAllMocks()
  })

  describe('Video review interface', () => {
    it('should fetch pending videos for review', async () => {
      localStorage.setItem('teri_token', 'admin-token')

      const mockPendingVideosResponse = {
        data: {
          code: 200,
          message: 'success',
          data: [
            {
              id: 1,
              title: 'Pending Video 1',
              userId: 10,
              username: 'user1',
              uploadedAt: '2024-01-01T00:00:00Z',
              status: 'pending'
            },
            {
              id: 2,
              title: 'Pending Video 2',
              userId: 11,
              username: 'user2',
              uploadedAt: '2024-01-01T00:01:00Z',
              status: 'pending'
            }
          ]
        }
      }

      axios.get.mockResolvedValueOnce(mockPendingVideosResponse)

      const result = await adminStore.fetchPendingVideos()

      // Verify API was called correctly
      expect(axios.get).toHaveBeenCalledWith('/api/admin/video/pending', {
        params: {},
        headers: {
          Authorization: 'Bearer admin-token'
        }
      })

      // Verify store state was updated
      expect(adminStore.pendingVideos).toHaveLength(2)
      expect(adminStore.pendingVideos[0].title).toBe('Pending Video 1')
      expect(adminStore.pendingVideos[1].title).toBe('Pending Video 2')
    })

    it('should approve a video', async () => {
      localStorage.setItem('teri_token', 'admin-token')

      adminStore.pendingVideos = [
        { id: 1, title: 'Video 1', status: 'pending' },
        { id: 2, title: 'Video 2', status: 'pending' }
      ]
      adminStore.statistics.pendingReviews = 2

      const mockApproveResponse = {
        data: {
          code: 200,
          message: '审核成功',
          data: {
            id: 1,
            status: 'approved'
          }
        }
      }

      axios.post.mockResolvedValueOnce(mockApproveResponse)

      const result = await adminStore.reviewVideo(1, 'approved')

      // Verify API was called correctly
      expect(axios.post).toHaveBeenCalledWith('/api/admin/video/1/review', {
        status: 'approved',
        reason: ''
      }, {
        headers: {
          Authorization: 'Bearer admin-token'
        }
      })

      // Verify video was removed from pending list
      expect(adminStore.pendingVideos).toHaveLength(1)
      expect(adminStore.pendingVideos[0].id).toBe(2)
      
      // Verify pending count was decremented
      expect(adminStore.statistics.pendingReviews).toBe(1)
    })

    it('should reject a video with reason', async () => {
      localStorage.setItem('teri_token', 'admin-token')

      adminStore.pendingVideos = [
        { id: 1, title: 'Video 1', status: 'pending' }
      ]
      adminStore.statistics.pendingReviews = 1

      const mockRejectResponse = {
        data: {
          code: 200,
          message: '审核成功',
          data: {
            id: 1,
            status: 'rejected'
          }
        }
      }

      axios.post.mockResolvedValueOnce(mockRejectResponse)

      await adminStore.reviewVideo(1, 'rejected', '内容违规')

      // Verify reason was included
      expect(axios.post).toHaveBeenCalledWith('/api/admin/video/1/review', {
        status: 'rejected',
        reason: '内容违规'
      }, {
        headers: {
          Authorization: 'Bearer admin-token'
        }
      })

      // Verify video was removed from pending list
      expect(adminStore.pendingVideos).toHaveLength(0)
      expect(adminStore.statistics.pendingReviews).toBe(0)
    })

    it('should handle review error', async () => {
      localStorage.setItem('teri_token', 'admin-token')

      const mockErrorResponse = {
        data: {
          code: 500,
          message: '审核失败',
          data: null
        }
      }

      axios.post.mockResolvedValueOnce(mockErrorResponse)

      await expect(adminStore.reviewVideo(1, 'approved')).rejects.toThrow()
    })
  })

  describe('User management', () => {
    it('should fetch user list', async () => {
      localStorage.setItem('teri_token', 'admin-token')

      const mockUserListResponse = {
        data: {
          code: 200,
          message: 'success',
          data: {
            list: [
              {
                id: 1,
                username: 'user1',
                email: 'user1@example.com',
                status: 'active',
                createdAt: '2024-01-01T00:00:00Z'
              },
              {
                id: 2,
                username: 'user2',
                email: 'user2@example.com',
                status: 'active',
                createdAt: '2024-01-02T00:00:00Z'
              }
            ],
            total: 100
          }
        }
      }

      axios.get.mockResolvedValueOnce(mockUserListResponse)

      const result = await adminStore.fetchUserList()

      // Verify API was called correctly
      expect(axios.get).toHaveBeenCalledWith('/api/admin/user/list', {
        params: {
          page: 1,
          pageSize: 20
        },
        headers: {
          Authorization: 'Bearer admin-token'
        }
      })

      // Verify store state was updated
      expect(adminStore.userList).toHaveLength(2)
      expect(adminStore.userList[0].username).toBe('user1')
      expect(adminStore.userPagination.total).toBe(100)
    })

    it('should ban a user', async () => {
      localStorage.setItem('teri_token', 'admin-token')

      adminStore.userList = [
        { id: 1, username: 'user1', status: 'active' },
        { id: 2, username: 'user2', status: 'active' }
      ]

      const mockBanResponse = {
        data: {
          code: 200,
          message: '封禁成功',
          data: {
            id: 1,
            status: 'banned'
          }
        }
      }

      axios.post.mockResolvedValueOnce(mockBanResponse)

      await adminStore.banUser(1, '违规行为', 7)

      // Verify API was called correctly
      expect(axios.post).toHaveBeenCalledWith('/api/admin/user/1/ban', {
        reason: '违规行为',
        duration: 7
      }, {
        headers: {
          Authorization: 'Bearer admin-token'
        }
      })

      // Verify user status was updated
      expect(adminStore.userList[0].status).toBe('banned')
    })

    it('should unban a user', async () => {
      localStorage.setItem('teri_token', 'admin-token')

      adminStore.userList = [
        { id: 1, username: 'user1', status: 'banned' }
      ]

      const mockUnbanResponse = {
        data: {
          code: 200,
          message: '解封成功',
          data: {
            id: 1,
            status: 'active'
          }
        }
      }

      axios.post.mockResolvedValueOnce(mockUnbanResponse)

      await adminStore.unbanUser(1)

      // Verify API was called correctly
      expect(axios.post).toHaveBeenCalledWith('/api/admin/user/1/unban', {}, {
        headers: {
          Authorization: 'Bearer admin-token'
        }
      })

      // Verify user status was updated
      expect(adminStore.userList[0].status).toBe('active')
    })

    it('should delete a user', async () => {
      localStorage.setItem('teri_token', 'admin-token')

      adminStore.userList = [
        { id: 1, username: 'user1' },
        { id: 2, username: 'user2' },
        { id: 3, username: 'user3' }
      ]
      adminStore.userPagination.total = 3

      const mockDeleteResponse = {
        data: {
          code: 200,
          message: '删除成功',
          data: {}
        }
      }

      axios.delete.mockResolvedValueOnce(mockDeleteResponse)

      await adminStore.deleteUser(2)

      // Verify API was called correctly
      expect(axios.delete).toHaveBeenCalledWith('/api/admin/user/2', {
        headers: {
          Authorization: 'Bearer admin-token'
        }
      })

      // Verify user was removed from list
      expect(adminStore.userList).toHaveLength(2)
      expect(adminStore.userList.find(u => u.id === 2)).toBeUndefined()
      expect(adminStore.userPagination.total).toBe(2)
    })

    it('should support user list pagination', async () => {
      localStorage.setItem('teri_token', 'admin-token')

      const mockResponse = {
        data: {
          code: 200,
          data: {
            list: [],
            total: 200
          }
        }
      }

      axios.get.mockResolvedValueOnce(mockResponse)

      adminStore.setUserPagination(3, 50)
      await adminStore.fetchUserList()

      expect(axios.get).toHaveBeenCalledWith('/api/admin/user/list', {
        params: {
          page: 3,
          pageSize: 50
        },
        headers: {
          Authorization: 'Bearer admin-token'
        }
      })
    })
  })

  describe('Statistics dashboard', () => {
    it('should fetch and display statistics', async () => {
      localStorage.setItem('teri_token', 'admin-token')

      const mockStatisticsResponse = {
        data: {
          code: 200,
          message: 'success',
          data: {
            totalUsers: 10000,
            totalVideos: 5000,
            totalComments: 50000,
            totalDanmu: 100000,
            pendingReviews: 25,
            todayNewUsers: 100,
            todayNewVideos: 50,
            todayViews: 10000
          }
        }
      }

      axios.get.mockResolvedValueOnce(mockStatisticsResponse)

      const result = await adminStore.fetchStatistics()

      // Verify API was called correctly
      expect(axios.get).toHaveBeenCalledWith('/api/admin/statistics', {
        headers: {
          Authorization: 'Bearer admin-token'
        }
      })

      // Verify statistics were updated
      expect(adminStore.statistics.totalUsers).toBe(10000)
      expect(adminStore.statistics.totalVideos).toBe(5000)
      expect(adminStore.statistics.totalComments).toBe(50000)
      expect(adminStore.statistics.totalDanmu).toBe(100000)
      expect(adminStore.statistics.pendingReviews).toBe(25)
      expect(adminStore.statistics.todayNewUsers).toBe(100)
      expect(adminStore.statistics.todayNewVideos).toBe(50)
      expect(adminStore.statistics.todayViews).toBe(10000)
    })

    it('should indicate when there are pending reviews', async () => {
      localStorage.setItem('teri_token', 'admin-token')

      const mockStatisticsResponse = {
        data: {
          code: 200,
          data: {
            totalUsers: 1000,
            totalVideos: 500,
            totalComments: 5000,
            totalDanmu: 10000,
            pendingReviews: 10,
            todayNewUsers: 5,
            todayNewVideos: 3,
            todayViews: 500
          }
        }
      }

      axios.get.mockResolvedValueOnce(mockStatisticsResponse)

      await adminStore.fetchStatistics()

      expect(adminStore.hasPendingReviews).toBe(true)
    })

    it('should indicate when there are no pending reviews', () => {
      adminStore.statistics.pendingReviews = 0

      expect(adminStore.hasPendingReviews).toBe(false)
    })

    it('should handle statistics fetch error', async () => {
      localStorage.setItem('teri_token', 'admin-token')

      const mockErrorResponse = {
        data: {
          code: 500,
          message: '服务器错误',
          data: null
        }
      }

      axios.get.mockResolvedValueOnce(mockErrorResponse)

      await expect(adminStore.fetchStatistics()).rejects.toThrow()
    })
  })

  describe('Admin authentication', () => {
    it('should require authentication for all admin actions', async () => {
      // No token set
      const mockErrorResponse = {
        response: {
          status: 401,
          data: {
            code: 401,
            message: '未授权'
          }
        }
      }

      axios.get.mockRejectedValueOnce(mockErrorResponse)

      await expect(adminStore.fetchStatistics()).rejects.toThrow()
    })

    it('should handle expired admin token', async () => {
      localStorage.setItem('teri_token', 'expired-token')

      const mockErrorResponse = {
        response: {
          status: 401,
          data: {
            code: 401,
            message: 'Token已过期'
          }
        }
      }

      axios.get.mockRejectedValueOnce(mockErrorResponse)

      await expect(adminStore.fetchPendingVideos()).rejects.toThrow()
    })
  })

  describe('System logs', () => {
    it('should fetch system logs', async () => {
      localStorage.setItem('teri_token', 'admin-token')

      const mockLogsResponse = {
        data: {
          code: 200,
          data: [
            {
              id: 1,
              action: 'user_login',
              userId: 10,
              timestamp: '2024-01-01T00:00:00Z',
              details: 'User logged in'
            },
            {
              id: 2,
              action: 'video_approved',
              adminId: 1,
              videoId: 5,
              timestamp: '2024-01-01T00:01:00Z',
              details: 'Video approved by admin'
            }
          ]
        }
      }

      axios.get.mockResolvedValueOnce(mockLogsResponse)

      const logs = await adminStore.fetchSystemLogs({ page: 1, pageSize: 20 })

      // Verify API was called correctly
      expect(axios.get).toHaveBeenCalledWith('/api/admin/logs', {
        params: { page: 1, pageSize: 20 },
        headers: {
          Authorization: 'Bearer admin-token'
        }
      })

      expect(logs).toHaveLength(2)
      expect(logs[0].action).toBe('user_login')
      expect(logs[1].action).toBe('video_approved')
    })
  })

  describe('Store utilities', () => {
    it('should reset user pagination', () => {
      adminStore.userPagination.page = 5
      adminStore.userPagination.total = 100

      adminStore.resetUserPagination()

      expect(adminStore.userPagination.page).toBe(1)
      expect(adminStore.userPagination.total).toBe(0)
    })

    it('should set user pagination', () => {
      adminStore.setUserPagination(3, 50)

      expect(adminStore.userPagination.page).toBe(3)
      expect(adminStore.userPagination.pageSize).toBe(50)
    })
  })
})
