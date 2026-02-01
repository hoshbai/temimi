/**
 * API module exports
 * Central export point for all API modules
 */

export { default as http } from './http'
export { toCamelCase, toSnakeCase } from './fieldMapper'
export { userApi } from './user'
export { videoApi } from './video'
export { commentApi } from './comment'
export { danmuApi } from './danmu'

// Default export with all APIs
export default {
  http: require('./http').default,
  userApi: require('./user').userApi,
  videoApi: require('./video').videoApi,
  commentApi: require('./comment').commentApi,
  danmuApi: require('./danmu').danmuApi
}
