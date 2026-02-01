package com.temimi.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.entity.User;
import com.temimi.model.entity.Video;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.SearchService;
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

    @Autowired
    private SearchService searchService;

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

    /**
     * 搜索视频（只返回已审核通过的）- 支持分页
     * GET /api/search/video/only-pass?keyword=关键词&page=1
     */
    @GetMapping("/video/only-pass")
    public ApiResult<?> searchVideos(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ApiResult.error("搜索关键词不能为空");
            }
            
            Page<?> result = searchService.searchVideos(keyword.trim(), page, 20);
            return ApiResult.success(result.getRecords());
        } catch (Exception e) {
            logger.error("搜索视频失败: {}", e.getMessage(), e);
            return ApiResult.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 搜索用户 - 支持分页
     * GET /api/search/user?keyword=关键词&page=1
     */
    @GetMapping("/user")
    public ApiResult<?> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ApiResult.error("搜索关键词不能为空");
            }
            
            Page<?> result = searchService.searchUsers(keyword.trim(), page, 20);
            return ApiResult.success(result.getRecords());
        } catch (Exception e) {
            logger.error("搜索用户失败: {}", e.getMessage(), e);
            return ApiResult.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 获取热搜词
     * GET /api/search/hot-keywords
     */
    @GetMapping("/hot-keywords")
    public ApiResult<?> getHotKeywords() {
        try {
            // TODO: 实现热搜词功能
            return ApiResult.success(java.util.Arrays.asList(
                "热门视频",
                "最新动态",
                "推荐UP主"
            ));
        } catch (Exception e) {
            logger.error("获取热搜词失败: {}", e.getMessage(), e);
            return ApiResult.error("获取热搜词失败: " + e.getMessage());
        }
    }

    /**
     * 获取搜索结果数量统计
     * GET /api/search/count?keyword=关键词
     */
    @GetMapping("/count")
    public ApiResult<?> getSearchCount(@RequestParam String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ApiResult.error("搜索关键词不能为空");
            }
            
            // 统计视频和用户数量
            Page<?> videoPage = searchService.searchVideos(keyword.trim(), 1, 1);
            Page<?> userPage = searchService.searchUsers(keyword.trim(), 1, 1);
            
            Map<String, Object> result = new HashMap<>();
            result.put("video", videoPage.getTotal());
            result.put("user", userPage.getTotal());
            
            return ApiResult.success(java.util.Arrays.asList(
                videoPage.getTotal(),
                userPage.getTotal()
            ));
        } catch (Exception e) {
            logger.error("获取搜索统计失败: {}", e.getMessage(), e);
            return ApiResult.error("获取搜索统计失败: " + e.getMessage());
        }
    }

    /**
     * 添加搜索词（记录搜索历史）
     * POST /api/search/word/add
     */
    @PostMapping("/word/add")
    public ApiResult<?> addSearchWord(@RequestParam String keyword) {
        try {
            // TODO: 实现搜索词记录功能
            logger.info("记录搜索词: {}", keyword);
            return ApiResult.success("记录成功");
        } catch (Exception e) {
            logger.error("记录搜索词失败: {}", e.getMessage(), e);
            return ApiResult.error("记录搜索词失败: " + e.getMessage());
        }
    }

    /**
     * 获取搜索词建议
     * GET /api/search/word/get?keyword=关键词
     */
    @GetMapping("/word/get")
    public ApiResult<?> getSearchWordSuggestions(@RequestParam String keyword) {
        try {
            // TODO: 实现搜索建议功能
            return ApiResult.success(java.util.Collections.emptyList());
        } catch (Exception e) {
            logger.error("获取搜索建议失败: {}", e.getMessage(), e);
            return ApiResult.error("获取搜索建议失败: " + e.getMessage());
        }
    }
}