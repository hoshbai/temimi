package com.temimi.controller.system;

import com.temimi.model.entity.User;
import com.temimi.model.entity.Video;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.UserService;
import com.temimi.service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局搜索控制器
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    /**
     * 综合搜索 (视频 + 用户)
     * GET /api/search?keyword=xxx
     */
    @GetMapping
    public ApiResult<Map<String, Object>> search(@RequestParam String keyword) {
        try {
            Map<String, Object> result = new HashMap<>();

            // 1. 搜索视频
            List<Video> videoList = videoService.searchVideos(keyword);
            result.put("videos", videoList);

            // 2. 搜索用户
            List<User> userList = userService.searchUsers(keyword);
            result.put("users", userList);

            return ApiResult.success(result);
        } catch (Exception e) {
            logger.error("搜索失败: {}", e.getMessage(), e);
            return ApiResult.error("搜索失败，请稍后重试");
        }
    }
}