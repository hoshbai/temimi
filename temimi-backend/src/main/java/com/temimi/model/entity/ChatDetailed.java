package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天记录表实体类
 * 对应数据库表: chat_detailed
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_detailed")
public class ChatDetailed {

    /**
     * 唯一主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 消息发送者
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 消息接收者
     */
    @TableField("another_id")
    private Integer anotherId;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 发送者是否删除
     */
    @TableField("user_del")
    private Boolean userDel;

    /**
     * 接受者是否删除
     */
    @TableField("another_del")
    private Boolean anotherDel;

    /**
     * 是否撤回
     */
    @TableField("withdraw")
    private Boolean withdraw;

    /**
     * 消息发送时间
     */
    @TableField("time")
    private LocalDateTime time;
}