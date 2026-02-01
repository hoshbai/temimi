package com.temimi.exception;

/**
 * 业务异常枚举 - 实现细粒度异常分类
 */
public enum BusinessErrorCode {
    
    // === 用户相关错误 (1xxx) ===
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ACCOUNT_BANNED(1002, "用户账号已被封禁"),
    USER_PERMISSION_DENIED(1003, "用户权限不足"),
    USER_LOGIN_FAILED(1004, "用户名或密码错误"),
    USER_ALREADY_EXISTS(1005, "用户名已存在"),
    
    // === 视频相关错误 (2xxx) ===
    VIDEO_NOT_FOUND(2001, "视频不存在"),
    VIDEO_STATUS_INVALID(2002, "视频状态无效"),
    VIDEO_UPLOAD_FAILED(2003, "视频上传失败"),
    VIDEO_FILE_INVALID(2004, "视频文件格式无效"),
    VIDEO_TITLE_EMPTY(2005, "视频标题不能为空"),
    VIDEO_CATEGORY_INVALID(2006, "视频分区无效"),
    VIDEO_DELETE_FAILED(2007, "视频删除失败"),
    PERMISSION_DENIED(2008, "权限不足"),
    
    // === 文件相关错误 (3xxx) ===
    FILE_UPLOAD_FAILED(3001, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(3002, "不支持的文件类型"),
    FILE_SIZE_EXCEEDED(3003, "文件大小超出限制"),
    FILE_NAME_INVALID(3004, "文件名格式错误"),
    
    // === 评论相关错误 (4xxx) ===
    COMMENT_NOT_FOUND(4001, "评论不存在"),
    COMMENT_DELETED(4002, "评论已被删除"),
    COMMENT_PARENT_NOT_FOUND(4003, "被回复的评论不存在"),
    
    // === 收藏相关错误 (5xxx) ===
    FAVORITE_NOT_FOUND(5001, "收藏夹不存在"),
    FAVORITE_PERMISSION_DENIED(5002, "收藏夹无权操作"),
    FAVORITE_VIDEO_EXISTS(5003, "视频已在收藏夹中"),
    
    // === 聊天相关错误 (6xxx) ===
    CHAT_MESSAGE_NOT_FOUND(6001, "聊天消息不存在"),
    CHAT_WITHDRAW_TIME_EXPIRED(6002, "消息撤回时间已过"),
    CHAT_PERMISSION_DENIED(6003, "聊天权限不足"),
    
    // === 投币相关错误 (7xxx) ===
    COIN_COUNT_INVALID(7001, "投币数量无效"),
    COIN_INSUFFICIENT(7002, "硬币数量不足"),
    COIN_LIMIT_EXCEEDED(7003, "投币数量超过限制"),

    // === 消息相关错误 (8xxx) ===
    MESSAGE_NOT_FOUND(8001, "消息不存在"),
    MESSAGE_PERMISSION_DENIED(8002, "无权操作该消息"),

    // === 系统错误 (9xxx) ===
    SYSTEM_ERROR(9001, "系统内部错误"),
    DATABASE_ERROR(9002, "数据库操作失败"),
    NETWORK_ERROR(9003, "网络连接错误"),
    UNAUTHORIZED(9004, "未授权的操作");
    
    private final int code;
    private final String message;
    
    BusinessErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}