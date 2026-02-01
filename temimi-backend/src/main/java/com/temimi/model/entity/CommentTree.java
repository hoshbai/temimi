package com.temimi.model.entity;

import lombok.Data;
import java.util.List;
import java.util.Map;

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
     * 根评论ID (0表示本身是根评论)
     */
    private Integer rootId;

    /**
     * 父评论ID (0表示直接回复视频)
     */
    private Integer parentId;

    /**
     * 被回复的用户ID
     */
    private Integer toUserId;

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
     * 点踩数
     */
    private Integer bad;

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

    /**
     * 完整的用户信息对象（包含exp等字段）
     */
    private Map<String, Object> user;

    /**
     * 子评论数量
     */
    private Integer count;

    /**
     * 回复列表（前端兼容字段）
     */
    private List<Map<String, Object>> replies;
}