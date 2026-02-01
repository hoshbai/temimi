package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户评论点赞点踩表实体类
 * 对应数据库表: user_comment_like
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_comment_like")
public class UserCommentLike {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    @TableField("uid")
    private Integer uid;

    /**
     * 评论ID
     */
    @TableField("comment_id")
    private Integer commentId;

    /**
     * 类型 1点赞 2点踩
     */
    @TableField("type")
    private Integer type;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 点赞类型常量
     */
    public static final Integer TYPE_LIKE = 1;

    /**
     * 点踩类型常量
     */
    public static final Integer TYPE_DISLIKE = 2;
}
