package com.temimi.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.entity.Video;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.SystemNotificationService;
import com.temimi.service.VideoService;
import com.temimi.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 视频审核控制器
 * ✅ 修复：所有接口都需要管理员权限
 */
@RestController
@RequestMapping("/api/admin")
public class VideoReviewController {

    private static final Logger logger = LoggerFactory.getLogger(VideoReviewController.class);

    @Autowired
    private VideoService videoService;

    @Autowired
    private SystemNotificationService systemNotificationService;

    /**
     * 获取待审核视频列表
     * GET /api/admin/videos/pending?pageNum=1&pageSize=10
     * ✅ 修复：添加管理员权限验证
     */
    @GetMapping("/videos/pending")
    public ApiResult<Page<Video>> getPendingVideos(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // ✅ 验证管理员权限
            SecurityUtil.requireAdmin();

            // 调用 VideoService，查询状态为 0 (审核中) 的视频
            Page<Video> page = videoService.getVideosByStatus(0, pageNum, pageSize);

            logger.info("管理员 {} 查询待审核视频列表，共 {} 条",
                SecurityUtil.getCurrentUserId(), page.getTotal());

            return ApiResult.success(page);
        } catch (SecurityException e) {
            logger.warn("非管理员尝试访问审核接口: {}", e.getMessage());
            return ApiResult.error(403, e.getMessage());
        } catch (Exception e) {
            logger.error("获取待审核视频列表失败", e);
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 审核通过
     * POST /api/admin/video/123/approve
     * ✅ 修复：添加管理员权限验证
     */
    @PostMapping("/video/{vid}/approve")
    public ApiResult<String> approveVideo(@PathVariable Integer vid) {
        try {
            // ✅ 验证管理员权限
            SecurityUtil.requireAdmin();

            // 先获取视频信息，用于发送通知
            Video video = videoService.getVideoDetail(vid);

            // 调用 VideoService，将视频状态更新为 1 (已过审)
            boolean success = videoService.updateVideoStatus(vid, 1);

            if (success) {
                logger.info("管理员 {} 审核通过视频 {}",
                    SecurityUtil.getCurrentUserId(), vid);
                
                // 发送系统通知给视频作者
                if (video != null) {
                    systemNotificationService.sendVideoApprovedNotification(
                        video.getUid(), vid, video.getTitle());
                }
                
                return ApiResult.success("审核通过");
            } else {
                logger.warn("审核通过视频 {} 失败", vid);
                return ApiResult.error("审核失败");
            }
        } catch (SecurityException e) {
            logger.warn("非管理员尝试审核视频 {}: {}", vid, e.getMessage());
            return ApiResult.error(403, e.getMessage());
        } catch (Exception e) {
            logger.error("审核通过视频 {} 时发生异常", vid, e);
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 审核拒绝
     * POST /api/admin/video/123/reject
     * ✅ 修复：添加管理员权限验证
     */
    @PostMapping("/video/{vid}/reject")
    public ApiResult<String> rejectVideo(
            @PathVariable Integer vid,
            @RequestParam(required = false) String reason) {
        try {
            // ✅ 验证管理员权限
            SecurityUtil.requireAdmin();

            // 先获取视频信息，用于发送通知
            Video video = videoService.getVideoDetail(vid);

            // 调用 VideoService，将视频状态更新为 2 (未通过)
            boolean success = videoService.updateVideoStatus(vid, 2);

            if (success) {
                logger.info("管理员 {} 拒绝视频 {}，原因: {}",
                    SecurityUtil.getCurrentUserId(), vid, reason != null ? reason : "无");
                
                // 发送系统通知给视频作者
                if (video != null) {
                    systemNotificationService.sendVideoRejectedNotification(
                        video.getUid(), vid, video.getTitle(), reason);
                }
                
                return ApiResult.success("审核拒绝");
            } else {
                logger.warn("拒绝视频 {} 失败", vid);
                return ApiResult.error("审核失败");
            }
        } catch (SecurityException e) {
            logger.warn("非管理员尝试拒绝视频 {}: {}", vid, e.getMessage());
            return ApiResult.error(403, e.getMessage());
        } catch (Exception e) {
            logger.error("审核拒绝视频 {} 时发生异常", vid, e);
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取所有视频列表（按状态筛选）
     * GET /api/admin/videos?status=1&pageNum=1&pageSize=10
     * ✅ 新增：管理员查看所有视频
     */
    @GetMapping("/videos")
    public ApiResult<Page<Video>> getVideosByStatus(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // ✅ 验证管理员权限
            SecurityUtil.requireAdmin();

            Page<Video> page;
            if (status != null) {
                page = videoService.getVideosByStatus(status, pageNum, pageSize);
            } else {
                page = videoService.getVideoList(pageNum, pageSize);
            }

            logger.info("管理员 {} 查询视频列表（状态: {}），共 {} 条",
                SecurityUtil.getCurrentUserId(), status, page.getTotal());

            return ApiResult.success(page);
        } catch (SecurityException e) {
            logger.warn("非管理员尝试访问视频管理接口: {}", e.getMessage());
            return ApiResult.error(403, e.getMessage());
        } catch (Exception e) {
            logger.error("获取视频列表失败", e);
            return ApiResult.error(e.getMessage());
        }
    }
}