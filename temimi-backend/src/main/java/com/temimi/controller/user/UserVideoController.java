package com.temimi.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.temimi.model.entity.UserVideo;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.UserVideoService;
import com.temimi.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户视频行为控制器
 */
@RestController
@RequestMapping("/api/user/video")
public class UserVideoController {

    @Autowired
    private UserVideoService userVideoService;

    /**
     * 获取用户对特定视频的行为记录
     * GET /api/user/video/behavior/{vid}
     * ✅ 修复：从 SecurityContext 获取 UID，防止伪造
     */
    @GetMapping("/behavior/{vid}")
    public ApiResult<UserVideo> getUserVideoBehavior(@PathVariable Integer vid) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", uid).eq("vid", vid);
            UserVideo userVideo = userVideoService.getOne(queryWrapper);
            return ApiResult.success(userVideo);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}