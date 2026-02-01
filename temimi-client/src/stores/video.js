import { defineStore } from 'pinia'
import axios from 'axios'
import { ElMessage } from 'element-plus'

export const useVideoStore = defineStore('video', {
  state: () => ({
    // 视频列表
    videoList: [],
    // 当前视频详情
    currentVideo: null,
    // 加载状态
    loading: false,
    // 分页信息
    pagination: {
      page: 1,
      pageSize: 20,
      total: 0,
    },
    // 用户与当前播放视频的互动数据 {love, unlove, coin, collect}
    attitudeToVideo: {},
    // 轮播图列表
    carousels: [],
    // 分区列表
    channels: [],
    // 热搜列表
    trendings: [],
    // 搜索到的相关数据数量 [视频, 用户]
    matchingCount: [0, 0],
    // 登录用户的收藏夹列表
    favorites: [],
  }),

  getters: {
    hasMore: (state) => {
      return state.videoList.length < state.pagination.total
    },
  },

  actions: {
    // 更新分区列表
    updateChannels(channels) {
      this.channels = channels
    },

    // 更新轮播图列表
    updateCarousels(carousels) {
      this.carousels = carousels
    },

    // 更新视频互动数据
    updateAttitudeToVideo(atv) {
      this.attitudeToVideo = atv
    },

    // 更新热搜列表
    updateTrendings(trendings) {
      this.trendings = trendings
    },

    // 更新搜索匹配数量
    updateMatchingCount(matchingCount) {
      this.matchingCount = matchingCount
    },

    // 获取视频列表
    async fetchVideoList(params = {}) {
      this.loading = true
      try {
        const result = await axios.get('/api/video/list', {
          params: {
            page: this.pagination.page,
            pageSize: this.pagination.pageSize,
            ...params,
          },
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code === 200) {
          const data = result.data.data
          this.videoList = data.list || []
          this.pagination.total = data.total || 0
          return data
        } else {
          ElMessage.error(result.data.message || '获取视频列表失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取视频列表失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 获取视频详情
    async fetchVideoDetail(videoId) {
      this.loading = true
      try {
        const result = await axios.get(`/api/video/detail/${videoId}`, {
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
          },
        })

        if (result.data.code === 200) {
          this.currentVideo = result.data.data
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '获取视频详情失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取视频详情失败:', error)
        throw error
      } finally {
        this.loading = false
      }
    },

    // 上传视频
    async uploadVideo(videoData) {
      try {
        const result = await axios.post('/api/video/upload', videoData, {
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
            'Content-Type': 'multipart/form-data',
          },
        })

        if (result.data.code === 200) {
          ElMessage.success('视频上传成功')
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '视频上传失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('视频上传失败:', error)
        throw error
      }
    },

    // 点赞视频
    async likeVideo(videoId) {
      try {
        const result = await axios.post(
          `/api/video/${videoId}/like`,
          {},
          {
            headers: {
              Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
            },
          }
        )

        if (result.data.code === 200) {
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '操作失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('点赞失败:', error)
        throw error
      }
    },

    // 收藏视频
    async collectVideo(videoId, favoriteId) {
      try {
        const result = await axios.post(
          `/api/video/${videoId}/collect`,
          { favoriteId },
          {
            headers: {
              Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
            },
          }
        )

        if (result.data.code === 200) {
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '收藏失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('收藏失败:', error)
        throw error
      }
    },

    // 投币
    async coinVideo(videoId, count) {
      try {
        const result = await axios.post(
          `/api/video/${videoId}/coin?count=${count}`,
          null,
          {
            headers: {
              Authorization: 'Bearer ' + localStorage.getItem('teri_token'),
            },
          }
        )

        if (result.data.code === 200) {
          return result.data.data
        } else {
          ElMessage.error(result.data.message || '投币失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('投币失败:', error)
        throw error
      }
    },

    // 获取随机推荐视频
    async fetchRandomVideos() {
      try {
        // Use the video list endpoint instead since random/visitor doesn't exist
        const result = await axios.get('/api/video/list', {
          params: {
            pageNum: 1,
            pageSize: 11
          }
        })

        if (result.data.code === 200) {
          // Transform the response to match expected format
          const videos = result.data.data.records || []
          return videos.map(video => ({
            video: {
              vid: video.vid,
              title: video.title,
              coverUrl: video.coverUrl,
              duration: video.duration,
              uploadDate: video.uploadTime
            },
            user: {
              uid: video.uid,
              nickname: video.uploaderName
            },
            stats: {
              play: video.playCount || 0,
              danmu: video.danmuCount || 0
            }
          }))
        } else {
          ElMessage.error(result.data.message || '获取推荐视频失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取推荐视频失败:', error)
        throw error
      }
    },

    // 获取累加推荐视频
    async fetchCumulativeVideos(vids) {
      try {
        // Calculate next page based on existing videos
        const currentPage = Math.floor(vids.split(',').length / 10) + 2
        
        const result = await axios.get('/api/video/list', {
          params: {
            pageNum: currentPage,
            pageSize: 10
          }
        })

        if (result.data.code === 200) {
          const videos = result.data.data.records || []
          const newVids = videos.map(v => v.vid)
          
          return {
            videos: videos.map(video => ({
              video: {
                vid: video.vid,
                title: video.title,
                coverUrl: video.coverUrl,
                duration: video.duration,
                uploadDate: video.uploadTime
              },
              user: {
                uid: video.uid,
                nickname: video.uploaderName
              },
              stats: {
                play: video.playCount || 0,
                danmu: video.danmuCount || 0
              }
            })),
            vids: newVids,
            more: result.data.data.current < result.data.data.pages
          }
        } else {
          ElMessage.error(result.data.message || '获取更多视频失败')
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取更多视频失败:', error)
        throw error
      }
    },

    // 获取用户投稿数
    async fetchUserWorksCount(uid) {
      try {
        const result = await axios.get('/api/video/user-works-count', {
          params: { uid },
        })

        if (result.data.code === 200) {
          return result.data.data
        } else {
          throw new Error(result.data.message)
        }
      } catch (error) {
        console.error('获取用户投稿数失败:', error)
        throw error
      }
    },

    // 设置分页
    setPagination(page, pageSize) {
      this.pagination.page = page
      if (pageSize) {
        this.pagination.pageSize = pageSize
      }
    },

    // 重置分页
    resetPagination() {
      this.pagination.page = 1
      this.pagination.total = 0
    },
  },
})
