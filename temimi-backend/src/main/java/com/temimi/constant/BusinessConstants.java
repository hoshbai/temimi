package com.temimi.constant;

/**
 * 业务常量类 - 避免魔法数字
 */
public class BusinessConstants {
    
    // === 用户状态常量 ===
    public static final int USER_STATE_NORMAL = 0;  // 正常
    public static final int USER_STATE_BANNED = 1;  // 封禁
    public static final int USER_STATE_DELETED = 2; // 注销
    
    // === 视频状态常量 ===
    public static final int VIDEO_STATUS_PENDING = 0;  // 审核中
    public static final int VIDEO_STATUS_APPROVED = 1; // 已过审
    public static final int VIDEO_STATUS_REJECTED = 2; // 未通过
    public static final int VIDEO_STATUS_DELETED = 3;  // 已删除
    
    // === 收藏夹类型常量 ===
    public static final int FAVORITE_TYPE_DEFAULT = 1; // 默认收藏夹
    public static final int FAVORITE_TYPE_CUSTOM = 2;  // 用户创建
    
    // === 收藏夹可见性常量 ===
    public static final int FAVORITE_VISIBLE_PRIVATE = 0; // 隐藏
    public static final int FAVORITE_VISIBLE_PUBLIC = 1;  // 公开
    
    // === 消息类型常量 ===
    public static final String MSG_TYPE_REPLY = "reply";
    public static final String MSG_TYPE_AT = "at";
    public static final String MSG_TYPE_LOVE = "love";
    public static final String MSG_TYPE_SYSTEM = "system";
    public static final String MSG_TYPE_WHISPER = "whisper";
    public static final String MSG_TYPE_DYNAMIC = "dynamic";
    
    // === 布尔值常量（用于数据库 tinyint 字段）===
    public static final Integer TRUE = 1;
    public static final Integer FALSE = 0;
    
    // === 投币数量常量 ===
    public static final int COIN_MIN_COUNT = 1;  // 最小投币数量
    public static final int COIN_MAX_COUNT = 2;  // 最大投币数量
    
    // === 用户行为状态常量 ===
    public static final int USER_ACTION_INACTIVE = 0;  // 未操作
    public static final int USER_ACTION_ACTIVE = 1;    // 已操作
    
    // === 撤回消息时间限制（分钟）===
    public static final int MESSAGE_WITHDRAW_TIME_LIMIT = 5;
    
    // === 统计初始值常量 ===
    public static final int STATS_INITIAL_VALUE = 0;
    
    // === 弹幕模式常量 ===
    public static final int DANMU_MODE_SCROLL = 1; // 滚动
    public static final int DANMU_MODE_TOP = 2;    // 顶部
    public static final int DANMU_MODE_BOTTOM = 3; // 底部
    
    // === 弹幕状态常量 ===
    public static final int DANMU_STATE_NORMAL = 1;   // 默认过审
    public static final int DANMU_STATE_PENDING = 2;  // 被举报审核中
    public static final int DANMU_STATE_DELETED = 3;  // 删除
    
    // === 文件路径常量 ===
    public static final String DEFAULT_COVER_PATH = "/default_cover.jpg";
    public static final String DEFAULT_AVATAR_PATH = "/default/avatar.png";
    
    // === 评论根节点ID ===
    public static final int COMMENT_ROOT_ID = 0;
}