package com.temimi.controller.video;

import com.temimi.model.vo.ApiResult;
import com.temimi.service.VideoBusinessService;
import com.temimi.util.SecurityUtil;
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
     * ✅ 修复：从 SecurityContext 获取 UID，防止伪造
     */
    @PostMapping("/like/{vid}")
    public ApiResult<String> likeVideo(@PathVariable Integer vid) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean success = videoBusinessService.likeVideo(uid, vid);
            return success ? ApiResult.success("点赞成功") : ApiResult.error("点赞失败");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 取消点赞视频
     * POST /api/video/interaction/unlike/{vid}
     * ✅ 修复：从 SecurityContext 获取 UID，防止伪造
     */
    @PostMapping("/unlike/{vid}")
    public ApiResult<String> unlikeVideo(@PathVariable Integer vid) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean success = videoBusinessService.unlikeVideo(uid, vid);
            return success ? ApiResult.success("取消点赞成功") : ApiResult.error("取消点赞失败");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 投币
     * POST /api/video/interaction/coin/{vid}?count=1
     * ✅ 修复：从 SecurityContext 获取 UID，防止伪造
     */
    @PostMapping("/coin/{vid}")
    public ApiResult<String> coinVideo(@PathVariable Integer vid, @RequestParam(defaultValue = "1") Integer count) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean success = videoBusinessService.coinVideo(uid, vid, count);
            return success ? ApiResult.success("投币成功") : ApiResult.error("投币失败");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 收藏视频
     * POST /api/video/interaction/collect/{vid}?fid=123
     * ✅ 修复：从 SecurityContext 获取 UID，防止伪造
     */
    @PostMapping("/collect/{vid}")
    public ApiResult<String> collectVideo(@PathVariable Integer vid, @RequestParam Integer fid) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean success = videoBusinessService.collectVideo(uid, vid, fid);
            return success ? ApiResult.success("收藏成功") : ApiResult.error("收藏失败");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 取消收藏视频
     * POST /api/video/interaction/uncollect/{vid}
     * ✅ 修复：从 SecurityContext 获取 UID，防止伪造
     */
    @PostMapping("/uncollect/{vid}")
    public ApiResult<String> uncollectVideo(@PathVariable Integer vid) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean success = videoBusinessService.uncollectVideo(uid, vid);
            return success ? ApiResult.success("取消收藏成功") : ApiResult.error("取消收藏失败");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 记录播放
     * POST /api/video/interaction/play/{vid}
     * ✅ 修复：从 SecurityContext 获取 UID，防止伪造
     */
    @PostMapping("/play/{vid}")
    public ApiResult<String> recordPlay(@PathVariable Integer vid) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean success = videoBusinessService.recordPlay(uid, vid);
            return success ? ApiResult.success("播放记录成功") : ApiResult.error("播放记录失败");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 点赞或取消点赞（前端兼容接口）
     * POST /api/video/love-or-not
     * 
     * @param vid 视频ID
     * @param isLove true=点赞/取消点赞, false=点踩/取消点踩
     * @param isSet true=设置, false=取消
     * @return 用户对该视频的态度信息
     */
    @PostMapping("/love-or-not")
    public ApiResult<java.util.Map<String, Object>> loveOrNot(
            @RequestParam Integer vid,
            @RequestParam Boolean isLove,
            @RequestParam Boolean isSet) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            
            // 根据参数决定操作
            boolean success = false;
            if (isLove && isSet) {
                // 点赞
                success = videoBusinessService.likeVideo(uid, vid);
            } else if (isLove && !isSet) {
                // 取消点赞
                success = videoBusinessService.unlikeVideo(uid, vid);
            } else {
                // 点踩功能暂未实现，返回成功
                success = true;
            }
            
            if (!success) {
                return ApiResult.error("操作失败");
            }
            
            // 返回用户对该视频的态度（简化版本）
            java.util.Map<String, Object> attitude = new java.util.HashMap<>();
            attitude.put("love", (isLove && isSet) ? 1 : 0);
            attitude.put("unlove", (!isLove && isSet) ? 1 : 0);
            attitude.put("coin", 0);  // 需要从数据库查询
            attitude.put("collect", 0);  // 需要从数据库查询
            
            return ApiResult.success(attitude);
            
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}