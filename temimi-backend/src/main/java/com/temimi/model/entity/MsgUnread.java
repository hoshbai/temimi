package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 消息未读数实体类
 * 对应数据库表: msg_unread
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("msg_unread")
public class MsgUnread {

    /**
     * 用户ID
     */
    @TableId("uid")
    private Integer uid;

    /**
     * 回复我的
     */
    @TableField("reply")
    private Integer reply;

    /**
     * @我的
     */
    @TableField("at")
    private Integer at;

    /**
     * 收到的赞
     */
    @TableField("love")
    private Integer love;

    /**
     * 系统通知
     */
    @TableField("system")
    private Integer system;

    /**
     * 我的消息
     */
    @TableField("whisper")
    private Integer whisper;

    /**
     * 动态
     */
    @TableField("dynamic")
    private Integer dynamic;
}