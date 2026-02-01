package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息表实体类
 * 对应数据库表: message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("message")
public class Message {

    /**
     * 消息ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 发送者UID
     */
    @TableField("from_uid")
    private Integer fromUid;

    /**
     * 接收者UID
     */
    @TableField("to_uid")
    private Integer toUid;

    /**
     * 消息类型 reply/like/system/follow
     */
    @TableField("type")
    private String type;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 关联目标类型 video/comment
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 关联目标ID
     */
    @TableField("target_id")
    private Integer targetId;

    /**
     * 关联内容摘要
     */
    @TableField("target_content")
    private String targetContent;

    /**
     * 是否已读 0未读 1已读
     */
    @TableField("is_read")
    private Boolean isRead;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
