package com.temimi.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.entity.Favorite;
import com.temimi.model.entity.Video;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.FavoriteService;
import com.temimi.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 收藏夹控制器
 */
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 获取用户的所有收藏夹
     * GET /api/favorite/list
     * ✅ 修复：从 SecurityContext 获取 UID，防止伪造
     */
    @GetMapping("/list")
    public ApiResult<List<Favorite>> getFavorites() {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            List<Favorite> favorites = favoriteService.getFavoritesByUid(uid);
            return ApiResult.success(favorites);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取指定用户的公开收藏夹（用于个人空间展示）
     * GET /api/favorite/user/{uid}
     */
    @GetMapping("/user/{uid}")
    public ApiResult<List<Favorite>> getUserPublicFavorites(@PathVariable Integer uid) {
        try {
            // 获取当前登录用户ID（可能为null，表示未登录）
            Integer currentUid = SecurityUtil.getCurrentUserId();

            // 如果是查看自己的收藏夹，返回所有收藏夹（包括私密）
            if (currentUid != null && currentUid.equals(uid)) {
                List<Favorite> favorites = favoriteService.getFavoritesByUid(uid);
                return ApiResult.success(favorites);
            }

            // 否则只返回公开的收藏夹
            List<Favorite> publicFavorites = favoriteService.getPublicFavoritesByUid(uid);
            return ApiResult.success(publicFavorites);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 创建收藏夹
     * POST /api/favorite/create
     * ✅ 修复：从 SecurityContext 获取 UID，防止伪造
     * ✅ 修复：支持 FormData 格式（前端使用 FormData 发送）
     */
    @PostMapping("/create")
    public ApiResult<Favorite> createFavorite(
            @RequestParam String title,
            @RequestParam(required = false, defaultValue = "") String desc,
            @RequestParam(required = false, defaultValue = "1") Integer visible) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            
            Favorite favorite = new Favorite();
            favorite.setTitle(title);
            favorite.setDescription(desc);
            favorite.setVisible(visible);
            
            boolean success = favoriteService.createFavorite(favorite, uid);
            if (success) {
                // 返回创建的收藏夹信息（包含自动生成的ID）
                return ApiResult.success(favorite);
            } else {
                return ApiResult.error("收藏夹创建失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 更新收藏夹信息
     * PUT /api/favorite/update
     *
     * 请求体示例：
     * {
     *   "fid": 123,
     *   "title": "新标题",
     *   "description": "新简介",
     *   "visible": 1
     * }
     */
    @PutMapping("/update")
    public ApiResult<String> updateFavorite(@RequestBody Favorite favorite) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean success = favoriteService.updateFavoriteInfo(favorite, uid);
            if (success) {
                return ApiResult.success("收藏夹更新成功");
            } else {
                return ApiResult.error("收藏夹更新失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 删除收藏夹 (软删除)
     * DELETE /api/favorite/delete/456
     * ✅ 修复：从 SecurityContext 获取 UID，防止伪造
     */
    @DeleteMapping("/delete/{fid}")
    public ApiResult<String> deleteFavorite(@PathVariable Integer fid) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean success = favoriteService.deleteFavorite(fid, uid);
            if (success) {
                return ApiResult.success("收藏夹删除成功");
            } else {
                return ApiResult.error("收藏夹删除失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    @Autowired
    private com.temimi.service.UserService userService;
    
    @Autowired
    private com.temimi.mapper.VideoStatsMapper videoStatsMapper;

    /**
     * 获取收藏夹内的视频列表（分页，包含作者信息）
     * GET /api/favorite/{fid}/videos?pageNum=1&pageSize=20
     */
    @GetMapping("/{fid}/videos")
    public ApiResult<Map<String, Object>> getFavoriteVideos(
            @PathVariable Integer fid,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            Page<Video> videoPage = favoriteService.getVideosInFavorite(fid, uid, pageNum, pageSize);
            
            // 转换为带作者信息的DTO
            java.util.List<Map<String, Object>> records = videoPage.getRecords().stream().map(video -> {
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("vid", video.getVid());
                item.put("title", video.getTitle());
                item.put("coverUrl", video.getCoverUrl());
                item.put("duration", video.getDuration());
                
                // 获取作者信息
                com.temimi.model.entity.User author = userService.getById(video.getUid());
                item.put("authorName", author != null ? author.getNickname() : "未知用户");
                item.put("authorAvatar", author != null ? author.getAvatar() : null);
                
                return item;
            }).collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("records", records);
            result.put("total", videoPage.getTotal());
            result.put("pages", videoPage.getPages());
            result.put("current", videoPage.getCurrent());
            
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 收藏视频到收藏夹（支持批量）
     * POST /api/favorite/collect
     *
     * 请求体示例：
     * {
     *   "vid": 123,
     *   "fids": [1, 2, 3]  // 收藏夹ID列表
     * }
     */
    @PostMapping("/collect")
    public ApiResult<String> collectVideo(@RequestBody Map<String, Object> params) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            Integer vid = (Integer) params.get("vid");

            @SuppressWarnings("unchecked")
            List<Integer> fids = (List<Integer>) params.get("fids");

            boolean success = favoriteService.collectVideoToFavorites(vid, fids, uid);
            if (success) {
                return ApiResult.success("收藏成功");
            } else {
                return ApiResult.error("部分收藏失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 从收藏夹移除视频
     * POST /api/favorite/uncollect
     *
     * 请求体示例：
     * {
     *   "vid": 123,
     *   "fid": 1
     * }
     */
    @PostMapping("/uncollect")
    public ApiResult<String> uncollectVideo(@RequestBody Map<String, Object> params) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            Integer vid = (Integer) params.get("vid");
            Integer fid = (Integer) params.get("fid");

            boolean success = favoriteService.removeVideoFromFavorite(vid, fid, uid);
            if (success) {
                return ApiResult.success("移除成功");
            } else {
                return ApiResult.error("移除失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 查询视频被收藏到哪些收藏夹
     * GET /api/favorite/collected-fids?vid=123
     *
     * 返回当前用户的收藏夹中，哪些收藏夹包含该视频
     */
    @GetMapping("/collected-fids")
    public ApiResult<List<Integer>> getCollectedFids(@RequestParam Integer vid) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            List<Integer> fids = favoriteService.getCollectedFids(vid, uid);
            return ApiResult.success(fids);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}