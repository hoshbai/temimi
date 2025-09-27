package com.temimi.controller.video;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.entity.Video;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    /**
     * 分页获取视频列表
     * GET /api/video/list?pageNum=1&pageSize=10
     */
    @GetMapping("/list")
    public ApiResult<Page<Video>> getVideoList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<Video> page = videoService.getVideoList(pageNum, pageSize);
            return ApiResult.success(page);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 根据视频ID获取视频详情
     * GET /api/video/detail/123
     */
    @GetMapping("/detail/{vid}")
    public ApiResult<Video> getVideoDetail(@PathVariable Integer vid) {
        try {
            Video video = videoService.getVideoDetail(vid);
            return ApiResult.success(video);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 根据子分区ID分页获取视频
     * GET /api/video/category?scId=cat&pageNum=1&pageSize=10
     */
    @GetMapping("/category")
    public ApiResult<Page<Video>> getVideosByCategory(
            @RequestParam String scId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<Video> page = videoService.getVideosByCategoryId(scId, pageNum, pageSize);
            return ApiResult.success(page);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}