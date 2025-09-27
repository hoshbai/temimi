package com.temimi.controller.video;

import com.temimi.model.entity.VideoStats;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.VideoStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 视频统计数据控制器
 */
@RestController
@RequestMapping("/api/video/stats")
public class VideoStatsController {

    @Autowired
    private VideoStatsService videoStatsService;

    /**
     * 获取视频统计数据
     * GET /api/video/stats/{vid}
     */
    @GetMapping("/{vid}")
    public ApiResult<VideoStats> getVideoStats(@PathVariable Integer vid) {
        try {
            VideoStats stats = videoStatsService.getStatsByVid(vid);
            if (stats == null) {
                return ApiResult.error("视频统计数据不存在");
            }
            return ApiResult.success(stats);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}