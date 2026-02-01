package com.temimi.controller.platform;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.entity.Comment;
import com.temimi.model.entity.Danmu;
import com.temimi.model.entity.Video;
import com.temimi.model.entity.User;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.CommentService;
import com.temimi.service.DanmuService;
import com.temimi.service.VideoService;
import com.temimi.service.UserService;
import com.temimi.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 创作中心 - 互动管理控制器
 * 用于管理用户视频的评论和弹幕
 */
@RestController
@RequestMapping("/api/platform/interaction")
public class InteractionController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private DanmuService danmuService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    /**
     * 获取用户视频的所有评论（分页）
     * GET /api/platform/interaction/comments?page=1&pageSize=20&vid=123&keyword=xxx
     */
    @GetMapping("/comments")
    public ApiResult<Map<String, Object>> getMyVideoComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer vid,
            @RequestParam(required = false) String keyword) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            
            // 获取用户的所有视频ID
            List<Integer> myVideoIds = getMyVideoIds(uid);
            if (myVideoIds.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("list", Collections.emptyList());
                result.put("total", 0);
                return ApiResult.success(result);
            }
            
            // 如果指定了vid，验证是否是用户的视频
            if (vid != null && !myVideoIds.contains(vid)) {
                return ApiResult.error("无权查看该视频的评论");
            }
            
            // 构建查询条件
            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
            if (vid != null) {
                queryWrapper.eq("vid", vid);
            } else {
                queryWrapper.in("vid", myVideoIds);
            }
            queryWrapper.eq("is_deleted", false);
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.like("content", keyword.trim());
            }
            queryWrapper.orderByDesc("create_time");
            
            // 分页查询
            Page<Comment> pageResult = commentService.page(new Page<>(page, pageSize), queryWrapper);
            
            // 填充用户信息和视频信息
            List<Map<String, Object>> commentList = new ArrayList<>();
            for (Comment comment : pageResult.getRecords()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", comment.getId());
                item.put("vid", comment.getVid());
                item.put("uid", comment.getUid());
                item.put("content", comment.getContent());
                item.put("love", comment.getLove());
                item.put("createTime", comment.getCreateTime());
                item.put("rootId", comment.getRootId());
                item.put("parentId", comment.getParentId());
                
                // 获取评论用户信息
                User commentUser = userService.getById(comment.getUid());
                if (commentUser != null) {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("uid", commentUser.getUid());
                    userInfo.put("nickname", commentUser.getNickname());
                    userInfo.put("avatar", commentUser.getAvatar());
                    item.put("user", userInfo);
                }
                
                // 获取视频信息
                Video video = videoService.getById(comment.getVid());
                if (video != null) {
                    Map<String, Object> videoInfo = new HashMap<>();
                    videoInfo.put("vid", video.getVid());
                    videoInfo.put("title", video.getTitle());
                    videoInfo.put("coverUrl", video.getCoverUrl());
                    item.put("video", videoInfo);
                }
                
                commentList.add(item);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", commentList);
            result.put("total", pageResult.getTotal());
            
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error("获取评论失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户视频的所有弹幕（分页）
     * GET /api/platform/interaction/danmus?page=1&pageSize=20&vid=123&keyword=xxx
     */
    @GetMapping("/danmus")
    public ApiResult<Map<String, Object>> getMyVideoDanmus(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer vid,
            @RequestParam(required = false) String keyword) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            
            // 获取用户的所有视频ID
            List<Integer> myVideoIds = getMyVideoIds(uid);
            if (myVideoIds.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("list", Collections.emptyList());
                result.put("total", 0);
                return ApiResult.success(result);
            }
            
            // 如果指定了vid，验证是否是用户的视频
            if (vid != null && !myVideoIds.contains(vid)) {
                return ApiResult.error("无权查看该视频的弹幕");
            }
            
            // 构建查询条件
            QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
            if (vid != null) {
                queryWrapper.eq("vid", vid);
            } else {
                queryWrapper.in("vid", myVideoIds);
            }
            queryWrapper.ne("state", 3); // 排除已删除的弹幕
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.like("content", keyword.trim());
            }
            queryWrapper.orderByDesc("create_date");
            
            // 分页查询
            Page<Danmu> pageResult = danmuService.page(new Page<>(page, pageSize), queryWrapper);
            
            // 填充用户信息和视频信息
            List<Map<String, Object>> danmuList = new ArrayList<>();
            for (Danmu danmu : pageResult.getRecords()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", danmu.getId());
                item.put("vid", danmu.getVid());
                item.put("uid", danmu.getUid());
                item.put("content", danmu.getContent());
                item.put("timePoint", danmu.getTimePoint());
                item.put("color", danmu.getColor());
                item.put("mode", danmu.getMode());
                item.put("createDate", danmu.getCreateDate());
                item.put("state", danmu.getState());
                
                // 获取弹幕用户信息
                User danmuUser = userService.getById(danmu.getUid());
                if (danmuUser != null) {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("uid", danmuUser.getUid());
                    userInfo.put("nickname", danmuUser.getNickname());
                    userInfo.put("avatar", danmuUser.getAvatar());
                    item.put("user", userInfo);
                }
                
                // 获取视频信息
                Video video = videoService.getById(danmu.getVid());
                if (video != null) {
                    Map<String, Object> videoInfo = new HashMap<>();
                    videoInfo.put("vid", video.getVid());
                    videoInfo.put("title", video.getTitle());
                    videoInfo.put("coverUrl", video.getCoverUrl());
                    item.put("video", videoInfo);
                }
                
                danmuList.add(item);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", danmuList);
            result.put("total", pageResult.getTotal());
            
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error("获取弹幕失败: " + e.getMessage());
        }
    }

    /**
     * 删除评论（UP主可以删除自己视频下的任何评论）
     * POST /api/platform/interaction/comment/delete
     */
    @PostMapping("/comment/delete")
    public ApiResult<String> deleteComment(@RequestParam Integer id) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            
            // 获取评论
            Comment comment = commentService.getById(id);
            if (comment == null) {
                return ApiResult.error("评论不存在");
            }
            
            // 验证权限：评论者本人或视频UP主可以删除
            Video video = videoService.getById(comment.getVid());
            if (video == null) {
                return ApiResult.error("视频不存在");
            }
            
            if (!comment.getUid().equals(uid) && !video.getUid().equals(uid)) {
                return ApiResult.error("无权删除该评论");
            }
            
            boolean success = commentService.deleteComment(id);
            return success ? ApiResult.success("删除成功") : ApiResult.error("删除失败");
        } catch (Exception e) {
            return ApiResult.error("删除评论失败: " + e.getMessage());
        }
    }

    /**
     * 删除弹幕（UP主可以删除自己视频下的任何弹幕）
     * POST /api/platform/interaction/danmu/delete
     */
    @PostMapping("/danmu/delete")
    public ApiResult<String> deleteDanmu(@RequestParam Integer id) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            
            // 获取弹幕
            Danmu danmu = danmuService.getById(id);
            if (danmu == null) {
                return ApiResult.error("弹幕不存在");
            }
            
            // 验证权限：弹幕发送者本人或视频UP主可以删除
            Video video = videoService.getById(danmu.getVid());
            if (video == null) {
                return ApiResult.error("视频不存在");
            }
            
            if (!danmu.getUid().equals(uid) && !video.getUid().equals(uid)) {
                return ApiResult.error("无权删除该弹幕");
            }
            
            boolean success = danmuService.deleteDanmu(id);
            return success ? ApiResult.success("删除成功") : ApiResult.error("删除失败");
        } catch (Exception e) {
            return ApiResult.error("删除弹幕失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的视频列表（用于筛选）
     * GET /api/platform/interaction/my-videos
     */
    @GetMapping("/my-videos")
    public ApiResult<List<Map<String, Object>>> getMyVideos() {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            
            QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", uid)
                       .eq("status", 1) // 只获取已审核通过的视频
                       .orderByDesc("upload_date");
            
            List<Video> videos = videoService.list(queryWrapper);
            
            List<Map<String, Object>> result = videos.stream().map(v -> {
                Map<String, Object> item = new HashMap<>();
                item.put("vid", v.getVid());
                item.put("title", v.getTitle());
                item.put("coverUrl", v.getCoverUrl());
                return item;
            }).collect(Collectors.toList());
            
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error("获取视频列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户所有视频的ID列表
     */
    private List<Integer> getMyVideoIds(Integer uid) {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).select("vid");
        return videoService.list(queryWrapper).stream()
                .map(Video::getVid)
                .collect(Collectors.toList());
    }
}
