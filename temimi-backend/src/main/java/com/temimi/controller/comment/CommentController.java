package com.temimi.controller.comment;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.entity.Comment;
import com.temimi.model.entity.CommentTree;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.CommentService;
import com.temimi.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 获取根评论列表（分页）
     * GET /api/comment/root?vid=123&pageNum=1&pageSize=10
     * ✅ 修复：返回Page对象而不是List
     */
    @GetMapping("/root")
    public ApiResult<Page<Comment>> getRootComments(
            @RequestParam Integer vid,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<Comment> page = commentService.getRootCommentsByVidPage(vid, pageNum, pageSize);
            return ApiResult.success(page);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取评论树列表（前端兼容接口）
     * GET /api/comment/get?vid=123&offset=0&type=1
     */
    @GetMapping("/get")
    public ApiResult<java.util.Map<String, Object>> getCommentTreeByVid(
            @RequestParam Integer vid,
            @RequestParam(defaultValue = "0") Long offset,
            @RequestParam(defaultValue = "1") Integer type) {
        try {
            // type: 1-按热度排序, 2-按时间排序
            List<CommentTree> commentTrees = commentService.getCommentTreesByVid(vid, offset, type);
            
            // 构造前端期望的数据结构
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("comments", commentTrees);
            result.put("more", commentTrees.size() >= 20); // 如果返回了20条，说明可能还有更多
            
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取UP主点赞的评论ID列表（前端兼容接口）
     * GET /api/comment/get-up-like?uid=123
     */
    @GetMapping("/get-up-like")
    public ApiResult<List<Integer>> getUpLike(@RequestParam Integer uid) {
        try {
            List<Integer> likedCommentIds = commentService.getUpLikedComments(uid);
            return ApiResult.success(likedCommentIds);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    @GetMapping("/tree/{rootId}")
    public ApiResult<CommentTree> getCommentTree(@PathVariable Integer rootId) {
        try {
            CommentTree commentTree = commentService.getCommentTreeByRootId(rootId);
            if (commentTree == null) {
                return ApiResult.error("评论不存在或已被删除");
            }
            return ApiResult.success(commentTree);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    @PostMapping("/post")
    public ApiResult<String> postComment(@RequestBody Comment comment) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean success = commentService.postComment(comment, uid);
            if (success) {
                return ApiResult.success("评论发布成功");
            } else {
                return ApiResult.error("评论发布失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 添加评论（前端兼容接口）
     * POST /api/comment/add
     */
    @PostMapping("/add")
    public ApiResult<java.util.Map<String, Object>> addComment(
            @RequestParam Integer vid,
            @RequestParam(defaultValue = "0") Integer root_id,
            @RequestParam(defaultValue = "0") Integer parent_id,
            @RequestParam Integer to_user_id,
            @RequestParam String content) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();

            // 添加日志记录
            org.slf4j.LoggerFactory.getLogger(CommentController.class).info(
                "收到评论请求 - uid: {}, vid: {}, root_id: {}, parent_id: {}, to_user_id: {}, content: {}",
                uid, vid, root_id, parent_id, to_user_id, content);

            Comment comment = new Comment();
            comment.setVid(vid);
            comment.setUid(uid);
            comment.setRootId(root_id);
            comment.setParentId(parent_id);
            comment.setToUserId(to_user_id);
            comment.setContent(content);

            boolean success = commentService.postComment(comment, uid);
            if (success) {
                // 构造前端期望的完整评论数据结构
                java.util.Map<String, Object> result = commentService.buildCommentResponse(comment);
                return ApiResult.success(result);
            } else {
                return ApiResult.error("评论发布失败");
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(CommentController.class).error(
                "发布评论异常: {}", e.getMessage(), e);
            return ApiResult.error(e.getMessage());
        }
    }

    @PostMapping("/like/{commentId}")
    public ApiResult<String> likeComment(@PathVariable Integer commentId) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean success = commentService.likeComment(commentId, uid);
            if (success) {
                return ApiResult.success("点赞成功");
            } else {
                return ApiResult.error("点赞失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    @PostMapping("/unlike/{commentId}")
    public ApiResult<String> unlikeComment(@PathVariable Integer commentId) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean success = commentService.unlikeComment(commentId, uid);
            if (success) {
                return ApiResult.success("取消点赞成功");
            } else {
                return ApiResult.error("取消点赞失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    @DeleteMapping("/{commentId}")
    public ApiResult<String> deleteComment(@PathVariable Integer commentId) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean isAdmin = SecurityUtil.isAdmin();

            Comment comment = commentService.getById(commentId);

            if (comment == null || Boolean.TRUE.equals(comment.getIsDeleted())) {
                return ApiResult.error("评论不存在或已被删除");
            }

            if (!isAdmin && !uid.equals(comment.getUid())) {
                return ApiResult.error("无权删除他人的评论");
            }

            boolean success = commentService.deleteComment(commentId);
            if (success) {
                return ApiResult.success("评论已删除");
            } else {
                return ApiResult.error("删除评论失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 删除评论（前端兼容接口 - POST方式）
     * POST /api/comment/delete
     */
    @PostMapping("/delete")
    public ApiResult<String> deleteCommentPost(@RequestParam Integer id) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean isAdmin = SecurityUtil.isAdmin();

            Comment comment = commentService.getById(id);

            if (comment == null || Boolean.TRUE.equals(comment.getIsDeleted())) {
                return ApiResult.error("评论不存在或已被删除");
            }

            if (!isAdmin && !uid.equals(comment.getUid())) {
                return ApiResult.error("无权删除他人的评论");
            }

            boolean success = commentService.deleteComment(id);
            if (success) {
                return ApiResult.success("评论已删除");
            } else {
                return ApiResult.error("删除评论失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 点赞或点踩评论（前端兼容接口）
     * POST /api/comment/love-or-not
     */
    @PostMapping("/love-or-not")
    public ApiResult<String> loveOrNot(
            @RequestParam Integer id,
            @RequestParam Boolean isLike,
            @RequestParam Boolean isSet) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();

            Comment comment = commentService.getById(id);
            if (comment == null || Boolean.TRUE.equals(comment.getIsDeleted())) {
                return ApiResult.error("评论不存在或已被删除");
            }

            // isLike: true-点赞, false-点踩
            // isSet: true-设置, false-取消
            if (isLike && isSet) {
                // 点赞
                boolean success = commentService.likeComment(id, uid);
                return success ? ApiResult.success("点赞成功") : ApiResult.error("点赞失败");
            } else if (isLike && !isSet) {
                // 取消点赞
                boolean success = commentService.unlikeComment(id, uid);
                return success ? ApiResult.success("取消点赞成功") : ApiResult.error("取消点赞失败");
            } else if (!isLike && isSet) {
                // 点踩
                boolean success = commentService.dislikeComment(id, uid);
                return success ? ApiResult.success("点踩成功") : ApiResult.error("点踩失败");
            } else {
                // 取消点踩
                boolean success = commentService.undislikeComment(id, uid);
                return success ? ApiResult.success("取消点踩成功") : ApiResult.error("取消点踩失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取用户在某个视频下的点赞/点踩列表
     * GET /api/comment/user-likes?vid=123
     */
    @GetMapping("/user-likes")
    public ApiResult<java.util.Map<String, Object>> getUserLikes(@RequestParam Integer vid) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();

            List<Integer> likedComments = commentService.getUserLikedComments(uid, vid);
            List<Integer> dislikedComments = commentService.getUserDislikedComments(uid, vid);

            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("likes", likedComments);
            result.put("dislikes", dislikedComments);

            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}