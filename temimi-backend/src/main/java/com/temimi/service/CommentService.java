package com.temimi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
     * 根据视频ID获取根评论列表（返回Page对象）
     * ✅ 新增方法：前端需要Page对象的total字段
     * @param vid 视频ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return Page对象包含评论列表和总数
     */
    Page<Comment> getRootCommentsByVidPage(Integer vid, int pageNum, int pageSize);

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

    /**
     * 点踩评论
     * @param commentId 评论ID
     * @param uid 用户ID
     * @return 是否点踩成功
     */
    boolean dislikeComment(Integer commentId, Integer uid);

    /**
     * 取消点踩评论
     * @param commentId 评论ID
     * @param uid 用户ID
     * @return 是否取消成功
     */
    boolean undislikeComment(Integer commentId, Integer uid);

    /**
     * 获取用户点赞的评论ID列表
     * @param uid 用户ID
     * @param vid 视频ID（可选）
     * @return 评论ID列表
     */
    List<Integer> getUserLikedComments(Integer uid, Integer vid);

    /**
     * 获取用户点踩的评论ID列表
     * @param uid 用户ID
     * @param vid 视频ID（可选）
     * @return 评论ID列表
     */
    List<Integer> getUserDislikedComments(Integer uid, Integer vid);

    /**
     * 删除评论（软删除）
     * @param commentId 评论ID
     * @return 是否删除成功
     */
    boolean deleteComment(Integer commentId);

    /**
     * 根据视频ID获取评论树列表
     * @param vid 视频ID
     * @param offset 偏移量
     * @param type 排序类型 1-按热度 2-按时间
     * @return 评论树列表
     */
    List<CommentTree> getCommentTreesByVid(Integer vid, Long offset, Integer type);

    /**
     * 获取UP主点赞的评论ID列表
     * @param uid UP主用户ID
     * @return 评论ID列表
     */
    List<Integer> getUpLikedComments(Integer uid);

    /**
     * 构建前端期望的评论响应数据结构
     * @param comment 评论对象
     * @return 包含用户信息的完整评论数据
     */
    java.util.Map<String, Object> buildCommentResponse(Comment comment);
}