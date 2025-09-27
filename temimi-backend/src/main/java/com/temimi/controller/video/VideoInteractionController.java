package com.temimi.controller.video;

import com.temimi.model.vo.ApiResult;
import com.temimi.service.VideoBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video/interaction")
public class VideoInteractionController {

    @Autowired
    private VideoBusinessService videoBusinessService;

    /**
     * 点赞视频
     * POST /api/video/interaction/like/{vid}
     */
    @PostMapping("/like/{vid}")
    public ApiResult<String> likeVideo(@PathVariable Integer vid, @RequestHeader("uid") Integer uid) {
        try {
            boolean success = videoBusinessService.likeVideo(uid, vid);
            return success ? ApiResult.success("点赞成功") : ApiResult.error("点赞失败");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 取消点赞视频
     * POST /api/video/interaction/unlike/{vid}
     */
    @PostMapping("/unlike/{vid}")
    public ApiResult<String> unlikeVideo(@PathVariable Integer vid, @RequestHeader("uid") Integer uid) {
        try {
            boolean success = videoBusinessService.unlikeVideo(uid, vid);
            return success ? ApiResult.success("取消点赞成功") : ApiResult.error("取消点赞失败");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 投币
     * POST /api/video/interaction/coin/{vid}?count=1
     */
    @PostMapping("/coin/{vid}")
    public ApiResult<String> coinVideo(@PathVariable Integer vid, @RequestHeader("uid") Integer uid, @RequestParam(defaultValue = "1") Integer count) {
        try {
            boolean success = videoBusinessService.coinVideo(uid, vid, count);
            return success ? ApiResult.success("投币成功") : ApiResult.error("投币失败");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 收藏视频
     * POST /api/video/interaction/collect/{vid}?fid=123
     */
    @PostMapping("/collect/{vid}")
    public ApiResult<String> collectVideo(@PathVariable Integer vid, @RequestHeader("uid") Integer uid, @RequestParam Integer fid) {
        try {
            boolean success = videoBusinessService.collectVideo(uid, vid, fid);
            return success ? ApiResult.success("收藏成功") : ApiResult.error("收藏失败");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 取消收藏视频
     * POST /api/video/interaction/uncollect/{vid}
     */
    @PostMapping("/uncollect/{vid}")
    public ApiResult<String> uncollectVideo(@PathVariable Integer vid, @RequestHeader("uid") Integer uid) {
        try {
            boolean success = videoBusinessService.uncollectVideo(uid, vid);
            return success ? ApiResult.success("取消收藏成功") : ApiResult.error("取消收藏失败");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 记录播放
     * POST /api/video/interaction/play/{vid}
     */
    @PostMapping("/play/{vid}")
    public ApiResult<String> recordPlay(@PathVariable Integer vid, @RequestHeader("uid") Integer uid) {
        try {
            boolean success = videoBusinessService.recordPlay(uid, vid);
            return success ? ApiResult.success("播放记录成功") : ApiResult.error("播放记录失败");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}