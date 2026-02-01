package com.temimi.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.entity.Category;
import com.temimi.model.entity.User;
import com.temimi.model.entity.Video;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.CategoryService;
import com.temimi.service.UserService;
import com.temimi.service.VideoService;
import com.temimi.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 审核管理控制器（管理后台兼容接口）
 * 处理 /api/review/* 的请求
 */
@RestController
@RequestMapping({"/api/review", "/review"})
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取指定状态的视频总数
     * GET /api/review/video/total?vstatus=0
     */
    @GetMapping("/video/total")
    public ApiResult<Long> getVideoTotal(@RequestParam Integer vstatus) {
        try {
            SecurityUtil.requireAdmin();
            
            QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", vstatus);
            long count = videoService.count(queryWrapper);
            
            return ApiResult.success(count);
        } catch (SecurityException e) {
            return ApiResult.error(403, e.getMessage());
        } catch (Exception e) {
            logger.error("获取视频总数失败", e);
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 分页获取指定状态的视频列表
     * GET /api/review/video/getpage?vstatus=0&page=1&quantity=10
     */
    @GetMapping("/video/getpage")
    public ApiResult<java.util.List<Map<String, Object>>> getVideoPage(
            @RequestParam Integer vstatus,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer quantity) {
        try {
            SecurityUtil.requireAdmin();
            
            Page<Video> videoPage = videoService.getVideosByStatus(vstatus, page, quantity);
            
            // 构建返回数据，包含视频、用户和分区信息
            java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
            for (Video video : videoPage.getRecords()) {
                Map<String, Object> item = new HashMap<>();
                item.put("video", video);
                
                // 获取用户信息
                User user = userService.getById(video.getUid());
                item.put("user", user);
                
                // 获取分区信息
                Map<String, String> categoryInfo = new HashMap<>();
                Category category = categoryService.getCategoryByScId(video.getScId());
                if (category != null) {
                    categoryInfo.put("mcId", category.getMcId());
                    categoryInfo.put("mcName", category.getMcName());
                    categoryInfo.put("scId", category.getScId());
                    categoryInfo.put("scName", category.getScName());
                } else {
                    categoryInfo.put("mcName", "未知分区");
                    categoryInfo.put("scName", "未知");
                }
                item.put("category", categoryInfo);
                
                result.add(item);
            }
            
            return ApiResult.success(result);
        } catch (SecurityException e) {
            return ApiResult.error(403, e.getMessage());
        } catch (Exception e) {
            logger.error("获取视频列表失败", e);
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取单个视频详情
     * GET /api/review/video/getone?vid=123
     */
    @GetMapping("/video/getone")
    public ApiResult<Map<String, Object>> getVideoDetail(@RequestParam Integer vid) {
        try {
            SecurityUtil.requireAdmin();
            
            Video video = videoService.getVideoDetail(vid);
            if (video == null) {
                return ApiResult.error("视频不存在");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("video", video);
            
            // 获取用户信息
            User user = userService.getById(video.getUid());
            result.put("user", user);
            
            // 获取分区信息
            Map<String, String> categoryInfo = new HashMap<>();
            Category category = categoryService.getCategoryByScId(video.getScId());
            if (category != null) {
                categoryInfo.put("mcId", category.getMcId());
                categoryInfo.put("mcName", category.getMcName());
                categoryInfo.put("scId", category.getScId());
                categoryInfo.put("scName", category.getScName());
            }
            result.put("category", categoryInfo);
            
            return ApiResult.success(result);
        } catch (SecurityException e) {
            return ApiResult.error(403, e.getMessage());
        } catch (Exception e) {
            logger.error("获取视频详情失败", e);
            return ApiResult.error(e.getMessage());
        }
    }
}
