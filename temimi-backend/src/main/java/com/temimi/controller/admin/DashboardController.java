package com.temimi.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.temimi.model.entity.User;
import com.temimi.model.entity.Video;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.UserService;
import com.temimi.service.VideoService;
import com.temimi.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台首页仪表盘控制器
 */
@RestController
@RequestMapping({"/api/dashboard", "/dashboard"})
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/stats")
    public ApiResult<Map<String, Object>> getStats() {
        try {
            SecurityUtil.requireAdmin();
            
            Map<String, Object> stats = new HashMap<>();
            
            // 今日开始时间
            LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            
            // 待审核视频数量
            QueryWrapper<Video> pendingQuery = new QueryWrapper<>();
            pendingQuery.eq("status", 0);
            long pendingVideos = videoService.count(pendingQuery);
            stats.put("pendingVideos", pendingVideos);
            
            // 已通过视频数量
            QueryWrapper<Video> approvedQuery = new QueryWrapper<>();
            approvedQuery.eq("status", 1);
            long approvedVideos = videoService.count(approvedQuery);
            stats.put("approvedVideos", approvedVideos);
            
            // 未通过视频数量
            QueryWrapper<Video> rejectedQuery = new QueryWrapper<>();
            rejectedQuery.eq("status", 2);
            long rejectedVideos = videoService.count(rejectedQuery);
            stats.put("rejectedVideos", rejectedVideos);
            
            // 总视频数量
            long totalVideos = videoService.count();
            stats.put("totalVideos", totalVideos);
            
            // 总用户数量
            long totalUsers = userService.count();
            stats.put("totalUsers", totalUsers);
            
            // 今日新增用户
            QueryWrapper<User> todayUserQuery = new QueryWrapper<>();
            todayUserQuery.ge("create_date", todayStart);
            long todayNewUsers = userService.count(todayUserQuery);
            stats.put("todayNewUsers", todayNewUsers);
            
            // 今日新增视频
            QueryWrapper<Video> todayVideoQuery = new QueryWrapper<>();
            todayVideoQuery.ge("upload_date", todayStart);
            long todayNewVideos = videoService.count(todayVideoQuery);
            stats.put("todayNewVideos", todayNewVideos);
            
            return ApiResult.success(stats);
        } catch (SecurityException e) {
            return ApiResult.error(403, e.getMessage());
        } catch (Exception e) {
            logger.error("获取仪表盘统计数据失败", e);
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取最近待审核的视频列表
     */
    @GetMapping("/recent-pending")
    public ApiResult<List<Map<String, Object>>> getRecentPending() {
        try {
            SecurityUtil.requireAdmin();
            
            QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 0)
                       .orderByDesc("upload_date")
                       .last("LIMIT 5");
            
            List<Video> videos = videoService.list(queryWrapper);
            
            List<Map<String, Object>> result = videos.stream().map(video -> {
                Map<String, Object> map = new HashMap<>();
                map.put("vid", video.getVid());
                map.put("title", video.getTitle());
                map.put("coverUrl", video.getCoverUrl());
                map.put("uploadDate", video.getUploadDate());
                
                // 获取用户信息
                User user = userService.getById(video.getUid());
                if (user != null) {
                    map.put("nickname", user.getNickname());
                    map.put("avatar", user.getAvatar());
                }
                return map;
            }).toList();
            
            return ApiResult.success(result);
        } catch (SecurityException e) {
            return ApiResult.error(403, e.getMessage());
        } catch (Exception e) {
            logger.error("获取最近待审核视频失败", e);
            return ApiResult.error(e.getMessage());
        }
    }
}
