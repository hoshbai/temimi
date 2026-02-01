import http from './http'

/**
 * 发布动态
 * @param {Object} data - { type, content, images, vid }
 */
export const publishDynamic = (data) => {
    return http.post('/api/dynamic/publish', data)
}

/**
 * 获取用户动态列表
 * @param {Number} uid - 用户ID
 * @param {Number} page - 页码
 * @param {Number} size - 每页数量
 * @param {Number} type - 动态类型筛选（可选，5=视频）
 */
export const getUserDynamics = (uid, page = 1, size = 10, type = null) => {
    const params = { page, size }
    if (type) params.type = type
    return http.get(`/api/dynamic/user/${uid}`, { params })
}

/**
 * 获取关注用户的动态流
 * @param {Number} page - 页码
 * @param {Number} size - 每页数量
 */
export const getFollowingDynamics = (page = 1, size = 10) => {
    return http.get('/api/dynamic/following', { params: { page, size } })
}

/**
 * 获取动态详情
 * @param {Number} id - 动态ID
 */
export const getDynamicDetail = (id) => {
    return http.get(`/api/dynamic/detail/${id}`)
}

/**
 * 点赞/取消点赞
 * @param {Number} id - 动态ID
 */
export const toggleDynamicLike = (id) => {
    return http.post(`/api/dynamic/like/${id}`)
}

/**
 * 删除动态
 * @param {Number} id - 动态ID
 */
export const deleteDynamic = (id) => {
    return http.delete(`/api/dynamic/${id}`)
}
