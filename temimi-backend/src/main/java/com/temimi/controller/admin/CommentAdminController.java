package com.temimi.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.entity.Comment;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员评论管理控制器
 */
@RestController
@RequestMapping("/api/admin/comment")
@PreAuthorize("hasRole('ADMIN')")
public class CommentAdminController {

    @Autowired
    private CommentService commentService;

    /**
     * 获取所有评论列表（分页）
     * GET /api/admin/comment/list?page=1&pageSize=20&videoId=123&keyword=xxx
     */
    @GetMapping("/list")
    public ApiResult<Page<Comment>> getCommentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer videoId,
            @RequestParam(required = false) String keyword) {
        try {
            Page<Comment> commentPage = new Page<>(page, pageSize);
            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
            
            // 只显示未删除的评论
            queryWrapper.eq("is_deleted", false);
            
            if (videoId != null) {
                queryWrapper.eq("vid", videoId);
            }
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.like("content", keyword);
            }
            
            queryWrapper.orderByDesc("comment_time");
            
            Page<Comment> result = commentService.page(commentPage, queryWrapper);
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error("获取评论列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定视频的评论列表
     * GET /api/admin/comment/video/123
     */
    @GetMapping("/video/{videoId}")
    public ApiResult<List<Comment>> getVideoComments(@PathVariable Integer videoId) {
        try {
            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("vid", videoId)
                       .eq("is_deleted", false)
                       .orderByDesc("comment_time");
            
            List<Comment> comments = commentService.list(queryWrapper);
            return ApiResult.success(comments);
        } catch (Exception e) {
            return ApiResult.error("获取视频评论失败: " + e.getMessage());
        }
    }

    /**
     * 删除评论
     * DELETE /api/admin/comment/{commentId}
     */
    @DeleteMapping("/{commentId}")
    public ApiResult<String> deleteComment(@PathVariable Integer commentId) {
        try {
            boolean success = commentService.deleteComment(commentId);
            if (success) {
                return ApiResult.success("删除成功");
            } else {
                return ApiResult.error("删除失败");
            }
        } catch (Exception e) {
            return ApiResult.error("删除评论失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除评论
     * POST /api/admin/comment/batch-delete
     */
    @PostMapping("/batch-delete")
    public ApiResult<String> batchDeleteComment(@RequestBody List<Integer> ids) {
        try {
            for (Integer id : ids) {
                commentService.deleteComment(id);
            }
            return ApiResult.success("批量删除成功");
        } catch (Exception e) {
            return ApiResult.error("批量删除评论失败: " + e.getMessage());
        }
    }
}
