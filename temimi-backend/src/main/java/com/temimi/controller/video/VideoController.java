package com.temimi.controller.video;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.dto.VideoDetailDto;
import com.temimi.model.entity.Video;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.SystemNotificationService;
import com.temimi.service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

    @Autowired
    private VideoService videoService;
    
    @Autowired
    private com.temimi.service.FavoriteService favoriteService;
    
    @Autowired
    private com.temimi.service.VideoBusinessService videoBusinessService;
    
    @Autowired
    private com.temimi.service.UserVideoService userVideoService;

    @Autowired
    private SystemNotificationService systemNotificationService;

    /**
     * 更新视频状态（管理后台兼容接口）
     * POST /api/video/change/status
     * 
     * @param vid 视频ID
     * @param status 新状态 (0=审核中, 1=已过审, 2=未通过, 3=已删除)
     * @return 操作结果
     */
    @PostMapping("/change/status")
    public ApiResult<String> changeVideoStatus(
            @RequestParam Integer vid,
            @RequestParam Integer status) {
        try {
            // ✅ 验证管理员权限
            com.temimi.util.SecurityUtil.requireAdmin();

            // 先获取视频信息，用于发送通知
            Video video = videoService.getVideoDetail(vid);

            // 调用 VideoService 更新视频状态
            boolean success = videoService.updateVideoStatus(vid, status);

            if (success) {
                logger.info("管理员 {} 更新视频 {} 状态为 {}",
                    com.temimi.util.SecurityUtil.getCurrentUserId(), vid, status);
                
                // 根据状态发送不同的系统通知
                if (video != null) {
                    switch (status) {
                        case 1: // 已过审
                            systemNotificationService.sendVideoApprovedNotification(
                                video.getUid(), vid, video.getTitle());
                            break;
                        case 2: // 未通过
                            systemNotificationService.sendVideoRejectedNotification(
                                video.getUid(), vid, video.getTitle(), null);
                            break;
                        case 3: // 已删除
                            systemNotificationService.sendVideoDeletedNotification(
                                video.getUid(), vid, video.getTitle());
                            break;
                    }
                }
                
                return ApiResult.success("操作成功");
            } else {
                logger.warn("更新视频 {} 状态失败", vid);
                return ApiResult.error("操作失败");
            }
        } catch (SecurityException e) {
            logger.warn("非管理员尝试更新视频状态 {}: {}", vid, e.getMessage());
            return ApiResult.error(403, e.getMessage());
        } catch (Exception e) {
            logger.error("更新视频 {} 状态时发生异常", vid, e);
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 收藏视频到收藏夹（前端兼容接口）
     * POST /api/video/collect
     * 
     * @param vid 视频ID
     * @param adds 要添加的收藏夹ID列表（逗号分隔）
     * @param removes 要移除的收藏夹ID列表（逗号分隔）
     * @return 操作结果
     */
    @PostMapping("/collect")
    public ApiResult<String> collectVideo(
            @RequestParam Integer vid,
            @RequestParam(required = false, defaultValue = "") String adds,
            @RequestParam(required = false, defaultValue = "") String removes) {
        try {
            Integer uid = com.temimi.util.SecurityUtil.getCurrentUserIdRequired();
            
            // 处理要添加的收藏夹
            if (!adds.isEmpty()) {
                String[] addArray = adds.split(",");
                for (String fidStr : addArray) {
                    if (!fidStr.trim().isEmpty()) {
                        try {
                            Integer fid = Integer.parseInt(fidStr.trim());
                            favoriteService.addVideoToFavorite(vid, fid, uid);
                        } catch (NumberFormatException e) {
                            // 忽略无效的ID
                        }
                    }
                }
            }
            
            // 处理要移除的收藏夹
            if (!removes.isEmpty()) {
                String[] removeArray = removes.split(",");
                for (String fidStr : removeArray) {
                    if (!fidStr.trim().isEmpty()) {
                        try {
                            Integer fid = Integer.parseInt(fidStr.trim());
                            favoriteService.removeVideoFromFavorite(vid, fid, uid);
                        } catch (NumberFormatException e) {
                            // 忽略无效的ID
                        }
                    }
                }
            }
            
            return ApiResult.success("操作成功");
            
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取用户对视频的交互状态（点赞、收藏、投币等）
     * GET /api/video/user-status?vid=123
     * 
     * @param vid 视频ID
     * @return 用户对该视频的交互状态
     */
    @GetMapping("/user-status")
    public ApiResult<java.util.Map<String, Object>> getUserVideoStatus(@RequestParam Integer vid) {
        try {
            Integer uid = com.temimi.util.SecurityUtil.getCurrentUserId();
            
            java.util.Map<String, Object> status = new java.util.HashMap<>();
            
            if (uid == null) {
                // 未登录用户，返回默认状态
                status.put("love", 0);
                status.put("unlove", 0);
                status.put("coin", 0);
                status.put("collect", 0);
            } else {
                // 查询用户对该视频的交互记录
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.temimi.model.entity.UserVideo> queryWrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                queryWrapper.eq("uid", uid).eq("vid", vid);
                
                com.temimi.model.entity.UserVideo userVideo = userVideoService.getOne(queryWrapper);
                
                if (userVideo == null) {
                    // 没有交互记录
                    status.put("love", 0);
                    status.put("unlove", 0);
                    status.put("coin", 0);
                    status.put("collect", 0);
                } else {
                    // 返回实际的交互状态
                    status.put("love", userVideo.getLove() != null ? userVideo.getLove() : 0);
                    status.put("unlove", userVideo.getUnlove() != null ? userVideo.getUnlove() : 0);
                    status.put("coin", userVideo.getCoin() != null ? userVideo.getCoin() : 0);
                    status.put("collect", userVideo.getCollect() != null ? userVideo.getCollect() : 0);
                }
            }
            
            return ApiResult.success(status);
            
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 投币（前端兼容接口）
     * POST /api/video/{vid}/coin
     * 
     * @param vid 视频ID
     * @param count 投币数量（1或2）
     * @return 操作结果
     */
    @PostMapping("/{vid}/coin")
    public ApiResult<String> coinVideo(
            @PathVariable Integer vid,
            @RequestParam(defaultValue = "1") Integer count) {
        try {
            Integer uid = com.temimi.util.SecurityUtil.getCurrentUserIdRequired();
            boolean success = videoBusinessService.coinVideo(uid, vid, count);
            
            if (success) {
                return ApiResult.success("投币成功");
            } else {
                return ApiResult.error("投币失败");
            }
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
            Integer uid = com.temimi.util.SecurityUtil.getCurrentUserIdRequired();
            
            // 查询或创建用户视频关联记录
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.temimi.model.entity.UserVideo> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            queryWrapper.eq("uid", uid).eq("vid", vid);
            com.temimi.model.entity.UserVideo userVideo = userVideoService.getOne(queryWrapper);
            
            if (userVideo == null) {
                // 创建新记录
                userVideo = new com.temimi.model.entity.UserVideo();
                userVideo.setUid(uid);
                userVideo.setVid(vid);
                userVideo.setPlay(0);
                userVideo.setLove(0);
                userVideo.setUnlove(0);
                userVideo.setCoin(0);
                userVideo.setCollect(0);
                userVideoService.save(userVideo);
            }
            
            // 记录操作前的点赞状态
            boolean hadLove = userVideo.getLove() != null && userVideo.getLove() == 1;
            
            // 根据参数决定操作
            boolean success = false;
            if (isLove && isSet) {
                // 点赞：取消"不喜欢"状态
                success = videoBusinessService.likeVideo(uid, vid);
                if (success) {
                    // 重新查询获取最新状态
                    userVideo = userVideoService.getOne(queryWrapper);
                    if (userVideo.getUnlove() != null && userVideo.getUnlove() == 1) {
                        userVideo.setUnlove(0);
                        userVideoService.updateById(userVideo);
                    }
                }
            } else if (isLove && !isSet) {
                // 取消点赞
                success = videoBusinessService.unlikeVideo(uid, vid);
            } else if (!isLove && isSet) {
                // 点"不喜欢"：如果之前点过赞，先取消点赞
                if (hadLove) {
                    videoBusinessService.unlikeVideo(uid, vid);
                    // 重新查询获取最新状态
                    userVideo = userVideoService.getOne(queryWrapper);
                }
                // 设置"不喜欢"状态
                userVideo.setUnlove(1);
                success = userVideoService.updateById(userVideo);
            } else {
                // 取消"不喜欢"
                userVideo.setUnlove(0);
                success = userVideoService.updateById(userVideo);
            }
            
            if (!success) {
                return ApiResult.error("操作失败");
            }
            
            // 返回更新后的用户对该视频的态度
            return getUserVideoStatus(vid);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 分页获取视频列表（包含用户和统计信息）
     * GET /api/video/list?pageNum=1&pageSize=10
     */
    @GetMapping("/list")
    public ApiResult<Page<VideoDetailDto>> getVideoList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<VideoDetailDto> page = videoService.getVideoListWithDetails(pageNum, pageSize);
            return ApiResult.success(page);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 根据视频ID获取视频详情（包含用户和统计信息）
     * GET /api/video/detail/123
     */
    @GetMapping("/detail/{vid}")
    public ApiResult<VideoDetailDto> getVideoDetail(@PathVariable Integer vid) {
        try {
            VideoDetailDto video = videoService.getVideoDetailWithInfo(vid);
            return ApiResult.success(video);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 根据子分区ID分页获取视频（包含用户和统计信息）
     * GET /api/video/category?scId=cat&pageNum=1&pageSize=10
     */
    @GetMapping("/category")
    public ApiResult<Page<VideoDetailDto>> getVideosByCategory(
            @RequestParam String scId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<VideoDetailDto> page = videoService.getVideosByCategoryWithDetails(scId, pageNum, pageSize);
            return ApiResult.success(page);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 根据用户ID分页获取用户投稿的视频（包含统计信息）
     * GET /api/video/user/123?pageNum=1&pageSize=10
     */
    @GetMapping("/user/{uid}")
    public ApiResult<Page<VideoDetailDto>> getUserVideos(
            @PathVariable Integer uid,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<VideoDetailDto> page = videoService.getVideosByUserWithDetails(uid, pageNum, pageSize);
            return ApiResult.success(page);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 查询视频被收藏到哪些收藏夹（前端兼容接口）
     * GET /api/video/collected-fids?vid=123
     */
    @GetMapping("/collected-fids")
    public ApiResult<java.util.List<Integer>> getCollectedFids(@RequestParam Integer vid) {
        try {
            Integer uid = com.temimi.util.SecurityUtil.getCurrentUserIdRequired();
            java.util.List<Integer> fids = favoriteService.getCollectedFids(vid, uid);
            return ApiResult.success(fids);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 记录用户播放视频（前端兼容接口）
     * POST /api/video/play/user?vid=123
     * 
     * ✅ 优化：添加防重复计数逻辑
     * - 同一用户在30分钟内多次观看同一视频，只计数一次
     * - 使用UserVideo表记录最后播放时间
     */
    @PostMapping("/play/user")
    public ApiResult<String> recordPlay(@RequestParam Integer vid) {
        try {
            Integer uid = com.temimi.util.SecurityUtil.getCurrentUserIdRequired();
            
            // 检查是否应该增加播放量（防止短时间内重复计数）
            boolean shouldIncrement = userVideoService.shouldIncrementPlayCount(uid, vid);
            
            if (shouldIncrement) {
                // 增加播放量
                videoService.incrementPlayCount(vid);
                // 更新用户的播放记录时间戳
                userVideoService.updatePlayTime(uid, vid);
                return ApiResult.success("播放记录成功");
            } else {
                return ApiResult.success("播放记录已存在（30分钟内）");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取用户投稿视频数量
     * GET /api/video/user-works-count?uid=123
     */
    @GetMapping("/user-works-count")
    public ApiResult<Integer> getUserWorksCount(@RequestParam Integer uid) {
        try {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.temimi.model.entity.Video> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            queryWrapper.eq("uid", uid)
                       .ne("status", com.temimi.constant.BusinessConstants.VIDEO_STATUS_DELETED);
            long count = videoService.count(queryWrapper);
            return ApiResult.success((int) count);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取用户投稿视频列表（分页）
     * GET /api/video/user-works?uid=123&rule=1&page=1&quantity=20
     * rule: 1-最新发布, 2-最多播放, 3-最多点赞
     */
    @GetMapping("/user-works")
    public ApiResult<java.util.Map<String, Object>> getUserWorks(
            @RequestParam Integer uid,
            @RequestParam(defaultValue = "1") Integer rule,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer quantity) {
        try {
            // 使用VideoService的方法获取带详细信息的视频列表
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.temimi.model.dto.UserVideoDto> detailPage = 
                videoService.getUserWorksWithDetails(uid, rule, page, quantity);
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("list", detailPage.getRecords());
            response.put("count", detailPage.getTotal());
            
            return ApiResult.success(response);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取用户点赞的视频列表
     * GET /api/video/user-love?uid=123&offset=0&quantity=20
     */
    @GetMapping("/user-love")
    public ApiResult<java.util.List<com.temimi.model.dto.UserVideoDto>> getUserLoveVideos(
            @RequestParam Integer uid,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer quantity) {
        try {
            java.util.List<com.temimi.model.dto.UserVideoDto> videos = videoService.getUserLoveVideos(uid, offset, quantity);
            return ApiResult.success(videos);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取用户投币的视频列表
     * GET /api/video/user-coin?uid=123&offset=0&quantity=20
     */
    @GetMapping("/user-coin")
    public ApiResult<java.util.List<com.temimi.model.dto.UserVideoDto>> getUserCoinVideos(
            @RequestParam Integer uid,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer quantity) {
        try {
            java.util.List<com.temimi.model.dto.UserVideoDto> videos = videoService.getUserCoinVideos(uid, offset, quantity);
            return ApiResult.success(videos);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取用户观看历史记录
     * GET /api/video/user-history?offset=0&quantity=20
     */
    @GetMapping("/user-history")
    public ApiResult<java.util.List<com.temimi.model.dto.UserVideoDto>> getUserHistory(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer quantity) {
        try {
            Integer uid = com.temimi.util.SecurityUtil.getCurrentUserIdRequired();
            java.util.List<com.temimi.model.dto.UserVideoDto> videos = videoService.getUserHistory(uid, offset, quantity);
            return ApiResult.success(videos);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取用户收藏的视频列表（所有收藏夹）
     * GET /api/video/user-favorites?offset=0&quantity=20
     */
    @GetMapping("/user-favorites")
    public ApiResult<java.util.List<com.temimi.model.dto.UserVideoDto>> getUserFavorites(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer quantity) {
        try {
            Integer uid = com.temimi.util.SecurityUtil.getCurrentUserIdRequired();
            java.util.List<com.temimi.model.dto.UserVideoDto> videos = videoService.getUserFavorites(uid, offset, quantity);
            return ApiResult.success(videos);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 删除用户观看历史记录
     * POST /api/video/delete-history
     * 
     * @param params 包含 vids 字段（视频ID列表）
     * @return 操作结果
     */
    @PostMapping("/delete-history")
    public ApiResult<String> deleteHistory(@RequestBody java.util.Map<String, Object> params) {
        try {
            Integer uid = com.temimi.util.SecurityUtil.getCurrentUserIdRequired();
            
            @SuppressWarnings("unchecked")
            java.util.List<Integer> vids = (java.util.List<Integer>) params.get("vids");
            
            if (vids == null || vids.isEmpty()) {
                return ApiResult.error("请选择要删除的历史记录");
            }
            
            // 删除用户的播放记录（将play设为0，清除play_time）
            for (Integer vid : vids) {
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.temimi.model.entity.UserVideo> queryWrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                queryWrapper.eq("uid", uid).eq("vid", vid);
                
                com.temimi.model.entity.UserVideo userVideo = userVideoService.getOne(queryWrapper);
                if (userVideo != null) {
                    userVideo.setPlay(0);
                    userVideo.setPlayTime(null);
                    userVideoService.updateById(userVideo);
                }
            }
            
            return ApiResult.success("删除成功");
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 删除视频（级联删除所有相关数据）
     * DELETE /api/video/delete/{vid}
     */
    @DeleteMapping("/delete/{vid}")
    public ApiResult<String> deleteVideo(@PathVariable Integer vid) {
        try {
            Integer uid = com.temimi.util.SecurityUtil.getCurrentUserIdRequired();
            
            // 使用新的级联删除方法，会删除视频及其所有相关数据（评论、弹幕、收藏等）
            boolean success = videoService.deleteVideoCompletely(vid, uid);
            
            if (success) {
                return ApiResult.success("删除成功");
            } else {
                return ApiResult.error("删除失败");
            }
        } catch (com.temimi.exception.BusinessException e) {
            return ApiResult.error(e.getMessage());
        } catch (Exception e) {
            return ApiResult.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 查询已上传的分片序号（断点续传）
     * GET /api/video/ask-chunk?hash=xxx
     */
    @GetMapping("/ask-chunk")
    public ApiResult<Integer> askChunk(@RequestParam String hash) {
        try {
            // 使用项目根目录的绝对路径
            String projectRoot = System.getProperty("user.dir");
            String tempDir = projectRoot + java.io.File.separator + "uploads" + java.io.File.separator + "temp" + java.io.File.separator + hash;
            java.io.File dir = new java.io.File(tempDir);
            
            if (!dir.exists()) {
                return ApiResult.success(0);
            }
            
            // 统计已上传的分片数量
            java.io.File[] files = dir.listFiles((d, name) -> name.startsWith("chunk_"));
            int uploadedCount = files != null ? files.length : 0;
            
            return ApiResult.success(uploadedCount);
        } catch (Exception e) {
            return ApiResult.success(0);
        }
    }

    /**
     * 上传视频分片
     * POST /api/video/upload-chunk
     */
    @PostMapping("/upload-chunk")
    public ApiResult<String> uploadChunk(
            @RequestParam("chunk") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("hash") String hash,
            @RequestParam("index") Integer index) {
        try {
            // 使用项目根目录的绝对路径
            String projectRoot = System.getProperty("user.dir");
            String tempDir = projectRoot + java.io.File.separator + "uploads" + java.io.File.separator + "temp" + java.io.File.separator + hash;
            java.io.File dir = new java.io.File(tempDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 保存分片文件
            String chunkFileName = "chunk_" + index;
            java.io.File chunkFile = new java.io.File(dir, chunkFileName);
            file.transferTo(chunkFile);
            
            return ApiResult.success("分片上传成功");
        } catch (Exception e) {
            return ApiResult.error("分片上传失败: " + e.getMessage());
        }
    }

    /**
     * 添加视频（上传完成后的元数据）
     * POST /api/video/add
     */
    @PostMapping("/add")
    public ApiResult<String> addVideo(
            @RequestParam("cover") org.springframework.web.multipart.MultipartFile cover,
            @RequestParam("hash") String hash,
            @RequestParam("title") String title,
            @RequestParam("type") Integer type,
            @RequestParam("auth") Integer auth,
            @RequestParam("duration") Double duration,
            @RequestParam("mcid") String mcid,
            @RequestParam("scid") String scid,
            @RequestParam("tags") String tags,
            @RequestParam("descr") String descr) {
        try {
            Integer uid = com.temimi.util.SecurityUtil.getCurrentUserIdRequired();
            
            // 使用配置的上传目录（与旧视频保持一致）
            String uploadBasePath = "D:/shiyou_upload/";
            
            // 1. 保存封面图片到 D:/shiyou_upload/covers/
            String coverDir = uploadBasePath + "covers";
            java.io.File coverDirFile = new java.io.File(coverDir);
            if (!coverDirFile.exists()) {
                coverDirFile.mkdirs();
            }
            
            String coverFileName = hash + ".jpg";
            java.io.File coverFile = new java.io.File(coverDir, coverFileName);
            cover.transferTo(coverFile);
            
            // 2. 合并视频分片到 D:/shiyou_upload/videos/
            String projectRoot = System.getProperty("user.dir");
            String tempDir = projectRoot + java.io.File.separator + "uploads" + java.io.File.separator + "temp" + java.io.File.separator + hash;
            String videoDir = uploadBasePath + "videos";
            java.io.File videoDirFile = new java.io.File(videoDir);
            if (!videoDirFile.exists()) {
                videoDirFile.mkdirs();
            }
            
            String videoFileName = hash + ".mp4";
            java.io.File videoFile = new java.io.File(videoDir, videoFileName);
            
            // 合并分片
            java.io.File tempDirFile = new java.io.File(tempDir);
            java.io.File[] chunks = tempDirFile.listFiles((d, name) -> name.startsWith("chunk_"));
            
            if (chunks != null && chunks.length > 0) {
                // 按序号排序
                java.util.Arrays.sort(chunks, (a, b) -> {
                    int indexA = Integer.parseInt(a.getName().substring(6));
                    int indexB = Integer.parseInt(b.getName().substring(6));
                    return indexA - indexB;
                });
                
                // 合并文件
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(videoFile)) {
                    for (java.io.File chunk : chunks) {
                        try (java.io.FileInputStream fis = new java.io.FileInputStream(chunk)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                        }
                    }
                }
                
                // 删除临时分片
                for (java.io.File chunk : chunks) {
                    chunk.delete();
                }
                tempDirFile.delete();
            }
            
            // 3. 创建视频记录（使用与旧视频一致的路径格式）
            Video video = new Video();
            video.setUid(uid);
            video.setTitle(title);
            video.setType(type);
            video.setAuth(auth);
            video.setDuration(duration);
            video.setMcId(mcid);
            video.setScId(scid);
            video.setTags(tags);
            video.setDescr(descr);
            video.setStatus(com.temimi.constant.BusinessConstants.VIDEO_STATUS_APPROVED);
            video.setUploadDate(java.time.LocalDateTime.now());
            // 使用与旧视频一致的路径格式：/videos/xxx.mp4 和 /covers/xxx.jpg
            video.setVideoUrl("/videos/" + videoFileName);
            video.setCoverUrl("/covers/" + coverFileName);
            
            // ✅ 修复：使用uploadVideo方法，会自动初始化video_stats表
            boolean success = videoService.uploadVideo(video, uid);
            
            if (success) {
                return ApiResult.success("视频投稿成功");
            } else {
                return ApiResult.error("视频投稿失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResult.error("视频投稿失败: " + e.getMessage());
        }
    }
}