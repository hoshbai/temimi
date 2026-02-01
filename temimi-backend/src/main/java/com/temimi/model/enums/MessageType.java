package com.temimi.model.enums;

/**
 * 消息类型枚举
 * 对应 msg_unread 表的各个字段
 */
public enum MessageType {
    /**
     * 回复我的
     */
    REPLY("reply", "回复"),

    /**
     * @我的
     */
    AT("at", "@我"),

    /**
     * 收到的赞
     */
    LOVE("love", "收到的赞"),

    /**
     * 系统通知
     */
    SYSTEM("system", "系统通知"),

    /**
     * 私信
     */
    WHISPER("whisper", "私信"),

    /**
     * 动态通知
     */
    DYNAMIC("dynamic", "动态");

    private final String code;
    private final String desc;

    MessageType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据code获取枚举
     */
    public static MessageType fromCode(String code) {
        for (MessageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
