package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 消息未读数表实体类
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
    @TableId(value = "uid", type = IdType.INPUT)
    private Integer uid;

    /**
     * 回复我的未读数
     */
    @TableField("reply")
    private Integer reply;

    /**
     * @我的未读数
     */
    @TableField("at")
    private Integer at;

    /**
     * 收到的赞未读数
     */
    @TableField("love")
    private Integer love;

    /**
     * 系统通知未读数
     */
    @TableField("`system`")
    private Integer system;

    /**
     * 私信未读数
     */
    @TableField("whisper")
    private Integer whisper;

    /**
     * 动态未读数
     */
    @TableField("`dynamic`")
    private Integer dynamic;
}
