package com.temimi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.temimi.model.entity.Comment;
import com.temimi.model.entity.CommentTree; // ✅ 使用 CommentTree

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService extends IService<Comment> {

    /**
     * 根据视频ID获取根评论列表（分页）
     * @param vid 视频ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 根评论列表
     */
    List<Comment> getRootCommentsByVid(Integer vid, int pageNum, int pageSize);

    /**
     * 根据根评论ID获取其所有子评论，构建评论树
     * @param rootId 根评论ID
     * @return 评论树
     */
    CommentTree getCommentTreeByRootId(Integer rootId); // ✅ 返回类型改为 CommentTree

    /**
     * 发布评论
     * @param comment 评论对象
     * @param uid 用户ID
     * @return 是否发布成功
     */
    boolean postComment(Comment comment, Integer uid);

    /**
     * 点赞评论
     * @param commentId 评论ID
     * @param uid 用户ID
     * @return 是否点赞成功
     */
    boolean likeComment(Integer commentId, Integer uid);

    /**
     * 取消点赞评论
     * @param commentId 评论ID
     * @param uid 用户ID
     * @return 是否取消成功
     */
    boolean unlikeComment(Integer commentId, Integer uid);
}