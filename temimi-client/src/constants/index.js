/**
 * 通用常量定义
 */

// API响应状态码
export const API_CODE = {
  SUCCESS: 200,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  SERVER_ERROR: 500,
}

// 消息类型
export const MESSAGE_TYPE = {
  REPLY: 0,      // 回复
  AT: 1,         // @提及
  LOVE: 2,       // 点赞
  SYSTEM: 3,     // 系统消息
  WHISPER: 4,    // 私信
  DYNAMIC: 5,    // 动态
}

// 消息类型名称映射
export const MESSAGE_TYPE_NAMES = {
  [MESSAGE_TYPE.REPLY]: '回复',
  [MESSAGE_TYPE.AT]: '@我的',
  [MESSAGE_TYPE.LOVE]: '收到的赞',
  [MESSAGE_TYPE.SYSTEM]: '系统通知',
  [MESSAGE_TYPE.WHISPER]: '私信',
  [MESSAGE_TYPE.DYNAMIC]: '动态',
}

// WebSocket 消息类型
export const WS_MESSAGE_TYPE = {
  ERROR: 'error',
  REPLY: 'reply',
  AT: 'at',
  LOVE: 'love',
  SYSTEM: 'system',
  WHISPER: 'whisper',
  DYNAMIC: 'dynamic',
}

// WebSocket 事件类型
export const WS_EVENT_TYPE = {
  ALL_READ: '全部已读',
  READ: '已读',
  REMOVE: '移除',
  RECEIVE: '接收',
  WITHDRAW: '撤回',
}

// 本地存储键名
export const STORAGE_KEYS = {
  TOKEN: 'teri_token',
  SEARCH_HISTORY: 'search_history',
  USER_INFO: 'user_info',
  THEME: 'theme',
}

// 分页配置
export const PAGINATION = {
  DEFAULT_PAGE: 1,
  DEFAULT_PAGE_SIZE: 20,
  VIDEO_PAGE_SIZE: 10,
  COMMENT_PAGE_SIZE: 20,
}

// 上传文件限制
export const UPLOAD_LIMITS = {
  MAX_VIDEO_SIZE: 500 * 1024 * 1024, // 500MB
  MAX_IMAGE_SIZE: 10 * 1024 * 1024,  // 10MB
  ALLOWED_VIDEO_TYPES: ['video/mp4', 'video/webm', 'video/ogg'],
  ALLOWED_IMAGE_TYPES: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
}

// 路由路径
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  VIDEO_DETAIL: '/video/:vid',
  USER_SPACE: '/space/:uid',
  SEARCH: '/search',
  MESSAGE: '/message',
  ACCOUNT: '/account',
  PLATFORM: '/platform',
}

// 视频态度类型
export const VIDEO_ATTITUDE = {
  COLLECT: 'collect',
  LIKE: 'like',
  LOVE: 'love',
  UNLOVE: 'unlove',
  COIN: 'coin',
}
