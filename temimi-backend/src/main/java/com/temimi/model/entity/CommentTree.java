package com.temimi.model.entity;

import lombok.Data;
import java.util.List;

/**
 * 评论树节点DTO
 * 用于构建前端展示的树形评论结构
 */
@Data
public class CommentTree {

    /**
     * 评论ID
     */
    private Integer id;

    /**
     * 视频ID
     */
    private Integer vid;

    /**
     * 评论者用户ID
     */
    private Integer uid;

    /**
     * 评论者昵称 (从User表关联查询)
     */
    private String nickname;

    /**
     * 评论者头像 (从User表关联查询)
     */
    private String avatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer love;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 子评论列表
     */
    private List<CommentTree> children;
}