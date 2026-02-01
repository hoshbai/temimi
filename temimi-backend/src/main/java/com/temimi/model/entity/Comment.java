package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论表实体类
 * 对应数据库表: comment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("comment")
public class Comment {

    /**
     * 评论主id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 评论的视频id
     */
    @TableField("vid")
    private Integer vid;

    /**
     * 发送者id
     */
    @TableField("uid")
    private Integer uid;

    /**
     * 根节点评论的id,如果为0表示为根节点
     */
    @TableField("root_id")
    private Integer rootId;

    /**
     * 被回复的评论id，只有root_id为0时才允许为0，表示根评论
     */
    @TableField("parent_id")
    private Integer parentId;

    /**
     * 回复目标用户id
     */
    @TableField("to_user_id")
    private Integer toUserId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 该条评论的点赞数
     */
    @TableField("love")
    private Integer love;

    /**
     * 不喜欢的数量
     */
    @TableField("bad")
    private Integer bad;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 是否置顶 0普通 1置顶
     */
    @TableField("is_top")
    private Boolean isTop;

    /**
     * 软删除 0未删除 1已删除
     */
    @TableField("is_deleted")
    private Boolean isDeleted;

    // ==================== 以下是非数据库字段，用于前端显示 ====================

    /**
     * 评论用户昵称（非数据库字段）
     * ✅ 新增：用于前端显示评论者昵称
     */
    @TableField(exist = false)
    private String username;

    /**
     * 评论用户头像URL（非数据库字段）
     * ✅ 新增：用于前端显示评论者头像
     */
    @TableField(exist = false)
    private String userAvatar;
}