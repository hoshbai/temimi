package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天表实体类
 * 对应数据库表: chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat")
public class Chat {

    /**
     * 唯一主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 对象UID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 用户UID
     */
    @TableField("another_id")
    private Integer anotherId;

    /**
     * 是否移除聊天 0否 1是
     */
    @TableField("is_deleted")
    private Boolean isDeleted;

    /**
     * 消息未读数量
     */
    @TableField("unread")
    private Integer unread;

    /**
     * 最近接收消息的时间或最近打开聊天窗口的时间
     */
    @TableField("latest_time")
    private LocalDateTime latestTime;
}