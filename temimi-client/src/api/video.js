// src/api/video.js

import request from '@/utils/request'

/**
 * 分页获取视频列表
 * @param {number} pageNum - 页码
 * @param {number} pageSize - 每页大小
 * @returns {Promise}
 */
export function getVideoList(pageNum = 1, pageSize = 10) {
  return request({
    url: '/video/list',
    method: 'get',
    params: {
      pageNum,
      pageSize
    }
  })
}

/**
 * 根据视频ID获取视频详情
 * @param {number} vid - 视频ID
 * @returns {Promise}
 */
export function getVideoDetail(vid) {
  return request({
    url: `/video/detail/${vid}`,
    method: 'get'
  })
}

/**
 * 根据子分区ID分页获取视频
 * @param {string} scId - 子分区ID
 * @param {number} pageNum - 页码
 * @param {number} pageSize - 每页大小
 * @returns {Promise}
 */
export function getVideosByCategory(scId, pageNum = 1, pageSize = 10) {
  return request({
    url: '/video/category',
    method: 'get',
    params: {
      scId,
      pageNum,
      pageSize
    }
  })
}

/**
 * 点赞视频
 * @param {number} vid - 视频ID
 * @returns {Promise}
 */
export function likeVideo(vid) {
  return request({
    url: `/video/interaction/like/${vid}`,
    method: 'post'
  })
}

/**
 * 取消点赞视频
 * @param {number} vid - 视频ID
 * @returns {Promise}
 */
export function unlikeVideo(vid) {
  return request({
    url: `/video/interaction/unlike/${vid}`,
    method: 'post'
  })
}

/**
 * 收藏视频
 * @param {number} vid - 视频ID
 * @returns {Promise}
 */
export function collectVideo(vid) {
  return request({
    url: `/video/interaction/collect/${vid}`,
    method: 'post'
  })
}

/**
 * 取消收藏视频
 * @param {number} vid - 视频ID
 * @returns {Promise}
 */
export function uncollectVideo(vid) {
  return request({
    url: `/video/interaction/uncollect/${vid}`,
    method: 'post'
  })
}

/**
 * 投币
 * @param {number} vid - 视频ID
 * @param {number} count - 投币数量 (1 or 2)
 * @returns {Promise}
 */
export function coinVideo(vid, count = 1) {
  return request({
    url: `/video/interaction/coin/${vid}`,
    method: 'post',
    params: {
      count
    }
  })
}