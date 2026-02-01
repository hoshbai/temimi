package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 动态表实体类
 * 对应数据库表: dynamic
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("dynamic")
public class Dynamic {

    /**
     * 动态ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 发布者用户ID
     */
    @TableField("uid")
    private Integer uid;

    /**
     * 动态类型 1纯文字 2图文 3转发视频 4转发文章 5投稿视频
     */
    @TableField("type")
    private Integer type;

    /**
     * 动态内容
     */
    @TableField("content")
    private String content;

    /**
     * 图片URL列表（JSON数组格式）
     */
    @TableField("images")
    private String images;

    /**
     * 关联的视频ID（转发视频时使用）
     */
    @TableField("vid")
    private Integer vid;

    /**
     * 点赞数
     */
    @TableField("love")
    private Integer love;

    /**
     * 评论数
     */
    @TableField("comment_count")
    private Integer commentCount;

    /**
     * 转发数
     */
    @TableField("forward_count")
    private Integer forwardCount;

    /**
     * 状态 0正常 1审核中 2已删除
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    // ==================== 非数据库字段 ====================

    /**
     * 发布者信息
     */
    @TableField(exist = false)
    private User user;

    /**
     * 关联的视频信息
     */
    @TableField(exist = false)
    private Video video;

    /**
     * 当前用户是否点赞动态
     */
    @TableField(exist = false)
    private Boolean isLiked;

    /**
     * 当前用户是否点赞视频（投稿视频动态使用）
     */
    @TableField(exist = false)
    private Boolean videoLiked;
}
