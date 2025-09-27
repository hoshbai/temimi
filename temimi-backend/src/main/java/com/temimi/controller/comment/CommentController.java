package com.temimi.controller.comment;

import com.temimi.model.entity.Comment;
import com.temimi.model.entity.CommentTree;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 根据视频ID获取根评论列表（分页）
     * GET /api/comment/root?vid=123&pageNum=1&pageSize=10
     */
    @GetMapping("/root")
    public ApiResult<List<Comment>> getRootComments(
            @RequestParam Integer vid,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            List<Comment> comments = commentService.getRootCommentsByVid(vid, pageNum, pageSize);
            return ApiResult.success(comments);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 根据根评论ID获取评论树
     * GET /api/comment/tree/456
     */
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

    /**
     * 发布评论
     * POST /api/comment/post
     */
    @PostMapping("/post")
    public ApiResult<String> postComment(@RequestBody Comment comment, @RequestHeader("uid") Integer uid) {
        try {
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
     * 点赞评论
     * POST /api/comment/like/789
     */
    @PostMapping("/like/{commentId}")
    public ApiResult<String> likeComment(@PathVariable Integer commentId, @RequestHeader("uid") Integer uid) {
        try {
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

    /**
     * 取消点赞评论
     * POST /api/comment/unlike/789
     */
    @PostMapping("/unlike/{commentId}")
    public ApiResult<String> unlikeComment(@PathVariable Integer commentId, @RequestHeader("uid") Integer uid) {
        try {
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
}