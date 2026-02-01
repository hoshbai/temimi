import http from './http'

/**
 * 关注/取消关注用户
 * @param {Number} followingId - 被关注用户ID
 * @param {String} action - 'follow' 或 'unfollow'
 */
export const followUser = (followingId, action) => {
    return http.post(`/api/user/follow/${followingId}?action=${action}`)
}

/**
 * 检查是否已关注某用户
 * @param {Number} followingId - 被关注用户ID
 */
export const checkFollowStatus = (followingId) => {
    return http.get(`/api/user/follow/status/${followingId}`)
}

/**
 * 获取用户的粉丝列表
 * @param {Number} uid - 用户ID
 * @param {Number} page - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getFansList = (uid, page = 1, pageSize = 20) => {
    return http.get(`/api/user/follow/fans/${uid}`, { params: { page, pageSize } })
}

/**
 * 获取用户的关注列表
 * @param {Number} uid - 用户ID
 * @param {Number} page - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getFollowingList = (uid, page = 1, pageSize = 20) => {
    return http.get(`/api/user/follow/following/${uid}`, { params: { page, pageSize } })
}

/**
 * 获取用户的粉丝和关注数统计
 * @param {Number} uid - 用户ID
 */
export const getFollowStats = (uid) => {
    return http.get(`/api/user/follow/stats/${uid}`)
}
