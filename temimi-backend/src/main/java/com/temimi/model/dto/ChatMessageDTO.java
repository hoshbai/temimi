package com.temimi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息DTO
 * 用于WebSocket消息传输
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    /**
     * 发送者用户ID
     */
    private Integer fromUid;
    
    /**
     * 接收者用户ID
     */
    private Integer toUid;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息时间戳
     */
    private Long timestamp;
}