package com.temimi.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.entity.Video;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin")
public class VideoReviewController {

    @Autowired
    private VideoService videoService;

    /**
     * 获取待审核视频列表
     * GET /api/admin/videos/pending?pageNum=1&pageSize=10
     */
    @GetMapping("/videos/pending")
    public ApiResult<Page<Video>> getPendingVideos(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 调用 VideoService，查询状态为 0 (审核中) 的视频
            Page<Video> page = videoService.getVideosByStatus(0, pageNum, pageSize);
            return ApiResult.success(page);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 审核通过
     * POST /api/admin/video/123/approve
     */
    @PostMapping("/video/{vid}/approve")
    public ApiResult<String> approveVideo(@PathVariable Integer vid) {
        try {
            // 调用 VideoService，将视频状态更新为 1 (已过审)
            boolean success = videoService.updateVideoStatus(vid, 1);
            return success ? ApiResult.success("审核通过") : ApiResult.error("审核失败");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 审核拒绝
     * POST /api/admin/video/123/reject
     */
    @PostMapping("/video/{vid}/reject")
    public ApiResult<String> rejectVideo(@PathVariable Integer vid) {
        try {
            // 调用 VideoService，将视频状态更新为 2 (未通过)
            boolean success = videoService.updateVideoStatus(vid, 2);
            return success ? ApiResult.success("审核拒绝") : ApiResult.error("审核失败");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}