package com.temimi.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandType {

    /**
     * 建立连接
     */
    CONNECTION(100),

    /**
     * 心跳包
     */
    HEARTBEAT(110),

    /**
     * 聊天功能 发送
     */
    CHAT_SEND(101),

    /**
     * 聊天功能 撤回
     */
    CHAT_WITHDRAW(102),

    /**
     * 弹幕功能 发送
     */
    DANMU_SEND(201),

    /**
     * 系统通知
     */
    SYSTEM_NOTIFICATION(401),

    /**
     * 错误响应
     */
    ERROR(-1),
    ;

    private final Integer code;

    public static CommandType match(Integer code) {
        for (CommandType value: CommandType.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return ERROR;
    }
}