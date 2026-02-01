import { defineStore } from 'pinia'
import http from '@/api/http'

export const useFavoriteStore = defineStore('favorite', {
  state: () => ({
    // 当前用户的收藏夹列表（用于收藏弹窗）
    favorites: [],
    // 用户空间的收藏夹列表（用于个人空间展示）
    userFavList: [],
  }),

  getters: {
    // 获取收藏夹数量
    favoritesCount: (state) => state.favorites.length,
    userFavListCount: (state) => state.userFavList.length,
  },

  actions: {
    // 设置当前用户的收藏夹列表
    setFavorites(favorites) {
      this.favorites = Array.isArray(favorites) ? favorites : []
    },

    // 设置用户空间的收藏夹列表
    setUserFavList(favList) {
      this.userFavList = Array.isArray(favList) ? favList : []
    },

    // 添加收藏夹
    addFavorite(favorite) {
      this.favorites.push(favorite)
    },

    // 删除收藏夹
    removeFavorite(fid) {
      const index = this.favorites.findIndex(f => f.fid === fid)
      if (index > -1) {
        this.favorites.splice(index, 1)
      }
    },

    // 更新收藏夹
    updateFavorite(fid, data) {
      const favorite = this.favorites.find(f => f.fid === fid)
      if (favorite) {
        Object.assign(favorite, data)
      }
    },

    // 清空收藏夹列表
    clearFavorites() {
      this.favorites = []
      this.userFavList = []
    },

    // 获取用户的收藏夹列表
    async fetchUserFavorites(uid) {
      try {
        const result = await http.get(`/api/favorite/user/${uid}`)
        if (result.data.code === 200) {
          this.setUserFavList(result.data.data)
          return result.data.data
        }
      } catch (error) {
        console.error('获取用户收藏夹失败:', error)
        throw error
      }
    },

    // 获取当前用户的收藏夹列表
    async fetchMyFavorites() {
      try {
        const result = await http.get('/api/favorite/list')
        if (result.data.code === 200) {
          this.setFavorites(result.data.data)
          return result.data.data
        }
      } catch (error) {
        console.error('获取我的收藏夹失败:', error)
        throw error
      }
    },
  },
})
