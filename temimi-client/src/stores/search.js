import { defineStore } from 'pinia'
import http from '@/api/http'
import { storage } from '@/utils/storage'

export const useSearchStore = defineStore('search', {
  state: () => ({
    // 搜索结果数量 [视频数, 用户数]
    matchingCount: [0, 0],
    // 热搜词列表
    trendings: [],
    // 搜索历史
    searchHistory: [],
    // 当前搜索关键词
    currentKeyword: '',
  }),

  getters: {
    // 视频搜索结果数量
    videoCount: (state) => state.matchingCount[0],
    // 用户搜索结果数量
    userCount: (state) => state.matchingCount[1],
    // 总搜索结果数量
    totalCount: (state) => state.matchingCount[0] + state.matchingCount[1],
  },

  actions: {
    // 设置搜索结果数量
    setMatchingCount(counts) {
      if (Array.isArray(counts) && counts.length === 2) {
        this.matchingCount = counts
      }
    },

    // 设置热搜词列表
    setTrendings(trendings) {
      this.trendings = Array.isArray(trendings) ? trendings : []
    },

    // 设置当前搜索关键词
    setCurrentKeyword(keyword) {
      this.currentKeyword = keyword
      // 添加到搜索历史
      if (keyword && !this.searchHistory.includes(keyword)) {
        this.searchHistory.unshift(keyword)
        // 限制历史记录数量
        if (this.searchHistory.length > 10) {
          this.searchHistory.pop()
        }
        // 保存到 storage
        this.saveSearchHistory()
      }
    },

    // 清空搜索结果数量
    clearMatchingCount() {
      this.matchingCount = [0, 0]
    },

    // 清空搜索历史
    clearSearchHistory() {
      this.searchHistory = []
      storage.clearSearchHistory()
    },

    // 从 storage 加载搜索历史
    loadSearchHistory() {
      this.searchHistory = storage.getSearchHistory()
    },

    // 保存搜索历史到 storage
    saveSearchHistory() {
      storage.setSearchHistory(this.searchHistory)
    },

    // 获取热搜词
    async fetchTrendings() {
      try {
        const result = await http.get('/api/search/trending')
        if (result.data.code === 200) {
          this.setTrendings(result.data.data)
          return result.data.data
        }
      } catch (error) {
        console.error('获取热搜词失败:', error)
        throw error
      }
    },
  },
})
