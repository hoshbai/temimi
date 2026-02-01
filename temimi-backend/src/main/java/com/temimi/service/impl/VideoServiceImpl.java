package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.constant.BusinessConstants;
import com.temimi.exception.BusinessException;
import com.temimi.exception.BusinessErrorCode;
import com.temimi.mapper.VideoMapper;
import com.temimi.mapper.VideoStatsMapper;
import com.temimi.model.dto.VideoDetailDto;
import com.temimi.model.entity.User;
import com.temimi.model.entity.Video;
import com.temimi.model.entity.VideoStats;
import com.temimi.service.DynamicService;
import com.temimi.service.UserService;
import com.temimi.service.VideoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    private static final Logger logger = LoggerFactory.getLogger(VideoServiceImpl.class);

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoStatsMapper videoStatsMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private com.temimi.service.CommentService commentService;

    @Autowired
    private com.temimi.service.DanmuService danmuService;

    @Autowired
    private com.temimi.service.CategoryService categoryService;

    @Autowired
    private com.temimi.service.UserVideoService userVideoService;

    @Autowired
    private com.temimi.mapper.FavoriteVideoMapper favoriteVideoMapper;

    @Autowired
    private com.temimi.service.FavoriteService favoriteService;

    @Autowired
    @Lazy
    private DynamicService dynamicService;

    @Value("${file.upload.path:D:/shiyou_upload/}")
    private String uploadBasePath;

    @Override
    public Page<Video> getVideoList(int pageNum, int pageSize) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        // 显示所有非删除状态的视频（包括审核中和已过审）
        queryWrapper.ne("status", BusinessConstants.VIDEO_STATUS_DELETED)
                .orderByDesc("upload_date");

        return videoMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Page<Video> getVideosByStatus(Integer status, int pageNum, int pageSize) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status)
                .orderByDesc("upload_date");
        return videoMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional
    public boolean updateVideoStatus(Integer vid, Integer status) {
        Video video = new Video();
        video.setVid(vid);
        video.setStatus(status);
        int result = videoMapper.updateById(video);
        
        // 如果审核通过，自动创建投稿视频动态
        if (result > 0 && status == BusinessConstants.VIDEO_STATUS_APPROVED) {
            Video fullVideo = videoMapper.selectById(vid);
            if (fullVideo != null) {
                try {
                    dynamicService.createVideoPublishDynamic(fullVideo.getUid(), vid);
                    logger.info("视频 {} 审核通过，已自动创建动态", vid);
                } catch (Exception e) {
                    logger.error("为视频 {} 创建动态失败", vid, e);
                    // 不影响审核结果，只记录日志
                }
            }
        }
        
        return result > 0;
    }

    @Override
    public Video getVideoDetail(Integer vid) {
        Video video = videoMapper.selectById(vid);
        if (video == null) {
            throw new BusinessException(BusinessErrorCode.VIDEO_NOT_FOUND);
        }

        // 允许查看审核中和已过审的视频，只拒绝已删除的视频
        if (video.getStatus() != null && video.getStatus().equals(BusinessConstants.VIDEO_STATUS_DELETED)) {
            throw new BusinessException(BusinessErrorCode.VIDEO_STATUS_INVALID, "视频已被删除");
        }

        return video;
    }
    
    @Override
    public List<Video> searchVideos(String keyword) {
        return videoMapper.searchVideos(keyword);
    }

    @Override
    @Transactional
    public boolean uploadVideo(Video video, Integer uid) {
        video.setUid(uid);
        video.setStatus(BusinessConstants.VIDEO_STATUS_PENDING);
        video.setUploadDate(LocalDateTime.now());
        
        int result = videoMapper.insert(video);
        
        if (result > 0) {
            initVideoStats(video.getVid());
        }
        return result > 0;
    }

    @Transactional
    @Override
    public String uploadVideo(MultipartFile videoFile, MultipartFile coverFile, String title, 
                             String scId, String tags, String descr, Integer uid) throws IOException {
        validateUploadParams(videoFile, title, scId);

        String videoFileName = this.saveFileToLocal(videoFile, "videos");

        String coverFileName = BusinessConstants.DEFAULT_COVER_PATH;
        if (coverFile != null && !coverFile.isEmpty()) {
            coverFileName = this.saveFileToLocal(coverFile, "covers");
        }

        Video video = buildVideoEntity(uid, title, scId, tags, descr, coverFileName, videoFileName);
        
        int result = videoMapper.insert(video);
        if (result <= 0) {
            throw new BusinessException(BusinessErrorCode.VIDEO_UPLOAD_FAILED, "视频信息保存失败");
        }

        initVideoStats(video.getVid());

        return video.getVideoUrl();
    }

    private void validateUploadParams(MultipartFile videoFile, String title, String scId) {
        if (videoFile == null || videoFile.isEmpty()) {
            throw new BusinessException(BusinessErrorCode.VIDEO_FILE_INVALID, "视频文件不能为空");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(BusinessErrorCode.VIDEO_TITLE_EMPTY);
        }
        if (scId == null || scId.trim().isEmpty()) {
            throw new BusinessException(BusinessErrorCode.VIDEO_CATEGORY_INVALID, "请选择视频分区");
        }
    }

    private Video buildVideoEntity(Integer uid, String title, String scId, String tags,
                                  String descr, String coverFileName, String videoFileName) {
        Video video = new Video();
        video.setUid(uid);
        video.setTitle(title);
        video.setScId(scId);

        // 安全的字符串分割操作
        String[] scIdParts = scId.split("_");
        if (scIdParts.length > 0) {
            video.setMcId(scIdParts[0]);
        } else {
            throw new BusinessException(BusinessErrorCode.VIDEO_CATEGORY_INVALID, "分区ID格式错误");
        }

        video.setTags(tags);
        video.setDescr(descr);
        video.setCoverUrl("/covers/" + coverFileName);
        video.setVideoUrl("/videos/" + videoFileName);
        
        // 获取视频时长
        try {
            String videoPath = uploadBasePath + "videos/" + videoFileName;
            Double duration = getVideoDuration(videoPath);
            video.setDuration(duration);
        } catch (Exception e) {
            logger.warn("获取视频时长失败: {}", e.getMessage());
            video.setDuration(0.0); // 默认值
        }
        
        // ✅ 修改：上传后自动通过审核，方便用户立即看到自己的视频
        video.setStatus(BusinessConstants.VIDEO_STATUS_APPROVED);
        video.setUploadDate(LocalDateTime.now());

        return video;
    }
    
    /**
     * 获取视频时长（秒）
     * 优先使用 Java 库（mp4parser），如果失败则尝试 FFmpeg
     */
    private Double getVideoDuration(String videoPath) {
        // 方法1: 使用 mp4parser 库（纯 Java，无需外部依赖）
        Double duration = getVideoDurationByJava(videoPath);
        if (duration != null && duration > 0) {
            return duration;
        }
        
        // 方法2: 如果 Java 库失败，尝试使用 FFmpeg（需要系统安装）
        duration = getVideoDurationByFFmpeg(videoPath);
        if (duration != null && duration > 0) {
            return duration;
        }
        
        // 如果都失败，返回默认值
        logger.warn("无法获取视频时长，使用默认值 0.0: {}", videoPath);
        return 0.0;
    }
    
    /**
     * 使用 mp4parser 库获取视频时长（支持 MP4 格式）
     */
    private Double getVideoDurationByJava(String videoPath) {
        try {
            File videoFile = new File(videoPath);
            if (!videoFile.exists()) {
                logger.warn("视频文件不存在: {}", videoPath);
                return null;
            }
            
            // 使用 mp4parser 读取 MP4 文件
            org.mp4parser.IsoFile isoFile = new org.mp4parser.IsoFile(videoPath);
            double lengthInSeconds = (double) isoFile.getMovieBox().getMovieHeaderBox().getDuration() 
                                   / isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
            isoFile.close();
            
            logger.info("成功通过 Java 库获取视频时长: {} 秒", lengthInSeconds);
            return lengthInSeconds;
        } catch (Exception e) {
            logger.debug("Java 库获取视频时长失败（可能不是 MP4 格式）: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 使用 FFmpeg 获取视频时长（支持所有格式，需要系统安装 FFmpeg）
     */
    private Double getVideoDurationByFFmpeg(String videoPath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                "ffprobe", "-v", "error", "-show_entries", 
                "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", 
                videoPath
            );
            Process process = processBuilder.start();
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream())
            );
            String line = reader.readLine();
            process.waitFor();
            
            if (line != null && !line.isEmpty()) {
                double duration = Double.parseDouble(line);
                logger.info("成功通过 FFmpeg 获取视频时长: {} 秒", duration);
                return duration;
            }
        } catch (Exception e) {
            logger.debug("FFmpeg 获取视频时长失败（可能未安装 FFmpeg）: {}", e.getMessage());
        }
        
        return null;
    }

    private void initVideoStats(Integer vid) {
        VideoStats stats = new VideoStats();
        stats.setVid(vid);
        stats.setPlay(BusinessConstants.STATS_INITIAL_VALUE);
        stats.setDanmu(BusinessConstants.STATS_INITIAL_VALUE);
        stats.setGood(BusinessConstants.STATS_INITIAL_VALUE);
        stats.setBad(BusinessConstants.STATS_INITIAL_VALUE);
        stats.setCoin(BusinessConstants.STATS_INITIAL_VALUE);
        stats.setCollect(BusinessConstants.STATS_INITIAL_VALUE);
        stats.setShare(BusinessConstants.STATS_INITIAL_VALUE);
        stats.setComment(BusinessConstants.STATS_INITIAL_VALUE);
        videoStatsMapper.insert(stats);
    }

    private String saveFileToLocal(MultipartFile file, String subDir) throws IOException {
        File dir = new File(uploadBasePath + subDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException(BusinessErrorCode.FILE_NAME_INVALID);
        }
        
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString().replace("-", "") + fileExtension;

        File destFile = new File(dir, newFilename);
        file.transferTo(destFile);

        return newFilename;
    }

    @Override
    public Page<Video> getVideosByCategoryId(String scId, int pageNum, int pageSize) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        // 显示所有非删除状态的视频（包括审核中和已过审）
        queryWrapper.eq("sc_id", scId)
                .ne("status", BusinessConstants.VIDEO_STATUS_DELETED)
                .orderByDesc("upload_date");

        return videoMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Page<VideoDetailDto> getVideoListWithDetails(int pageNum, int pageSize) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", BusinessConstants.VIDEO_STATUS_APPROVED) // 只显示已过审的视频
                .orderByDesc("upload_date");

        Page<Video> videoPage = videoMapper.selectPage(page, queryWrapper);
        return convertToDetailPage(videoPage);
    }

    @Override
    public VideoDetailDto getVideoDetailWithInfo(Integer vid) {
        Video video = videoMapper.selectById(vid);
        if (video == null) {
            throw new BusinessException(BusinessErrorCode.VIDEO_NOT_FOUND);
        }

        if (video.getStatus() != null && video.getStatus().equals(BusinessConstants.VIDEO_STATUS_DELETED)) {
            throw new BusinessException(BusinessErrorCode.VIDEO_STATUS_INVALID, "视频已被删除");
        }

        return convertToDetailDto(video);
    }

    @Override
    public Page<VideoDetailDto> getVideosByCategoryWithDetails(String scId, int pageNum, int pageSize) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sc_id", scId)
                .eq("status", BusinessConstants.VIDEO_STATUS_APPROVED) // 只显示已过审的视频
                .orderByDesc("upload_date");

        Page<Video> videoPage = videoMapper.selectPage(page, queryWrapper);
        return convertToDetailPage(videoPage);
    }

    @Override
    public Page<VideoDetailDto> getVideosByUserWithDetails(Integer uid, int pageNum, int pageSize) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                .ne("status", BusinessConstants.VIDEO_STATUS_DELETED) // 排除已删除的视频
                .orderByDesc("upload_date");

        Page<Video> videoPage = videoMapper.selectPage(page, queryWrapper);
        return convertToDetailPage(videoPage);
    }

    /**
     * 将Video分页结果转换为VideoDetailDto分页结果
     */
    private Page<VideoDetailDto> convertToDetailPage(Page<Video> videoPage) {
        Page<VideoDetailDto> detailPage = new Page<>(videoPage.getCurrent(), videoPage.getSize(), videoPage.getTotal());
        List<VideoDetailDto> detailList = videoPage.getRecords().stream()
                .map(this::convertToDetailDto)
                .collect(Collectors.toList());
        detailPage.setRecords(detailList);
        return detailPage;
    }

    /**
     * 将Video转换为VideoDetailDto
     */
    private VideoDetailDto convertToDetailDto(Video video) {
        // 查询用户信息（使用安全版本，不抛异常）
        User user = userService.getUserByIdSafe(video.getUid());
        String uploaderName = user != null ? user.getNickname() : "未知UP主";
        String uploaderAvatar = user != null ? user.getAvatar() : null;

        // 查询统计信息
        VideoStats stats = videoStatsMapper.selectById(video.getVid());

        // 查询分区信息
        String mcName = null;
        String scName = null;
        if (video.getScId() != null) {
            com.temimi.model.entity.Category category = categoryService.getCategoryByScId(video.getScId());
            if (category != null) {
                mcName = category.getMcName();
                scName = category.getScName();
            }
        }

        return VideoDetailDto.from(video, uploaderName, uploaderAvatar, stats, mcName, scName);
    }

    @Override
    public void incrementPlayCount(Integer vid) {
        VideoStats stats = videoStatsMapper.selectById(vid);
        if (stats != null) {
            stats.setPlay(stats.getPlay() + 1);
            videoStatsMapper.updateById(stats);
        }
    }

    @Override
    public Page<com.temimi.model.dto.UserVideoDto> getUserWorksWithDetails(Integer uid, Integer rule, int pageNum, int pageSize) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                .eq("status", BusinessConstants.VIDEO_STATUS_APPROVED); // 只显示已过审的视频
        
        // 根据规则排序
        switch (rule) {
            case 2:
                // 按播放量排序 - 需要关联video_stats表
                // 这里先用上传时间排序，后续可以优化为关联查询
                queryWrapper.orderByDesc("upload_date");
                break;
            case 3:
                // 按点赞量排序 - 需要关联video_stats表
                // 这里先用上传时间排序，后续可以优化为关联查询
                queryWrapper.orderByDesc("upload_date");
                break;
            case 1:
            default:
                // 默认按最新发布排序
                queryWrapper.orderByDesc("upload_date");
                break;
        }

        Page<Video> videoPage = videoMapper.selectPage(page, queryWrapper);
        
        // 转换为UserVideoDto
        List<com.temimi.model.dto.UserVideoDto> userVideoList = videoPage.getRecords().stream()
                .map(this::convertToUserVideoDto)
                .collect(Collectors.toList());
        
        // 如果需要按播放量或点赞量排序，在这里对结果进行二次排序
        if (rule == 2) {
            // 按播放量降序排序
            userVideoList.sort((a, b) -> {
                Integer playA = a.getStats() != null ? a.getStats().getPlay() : 0;
                Integer playB = b.getStats() != null ? b.getStats().getPlay() : 0;
                return playB.compareTo(playA);
            });
        } else if (rule == 3) {
            // 按点赞量降序排序
            userVideoList.sort((a, b) -> {
                Integer goodA = a.getStats() != null ? a.getStats().getGood() : 0;
                Integer goodB = b.getStats() != null ? b.getStats().getGood() : 0;
                return goodB.compareTo(goodA);
            });
        }
        
        Page<com.temimi.model.dto.UserVideoDto> resultPage = new Page<>(videoPage.getCurrent(), videoPage.getSize(), videoPage.getTotal());
        resultPage.setRecords(userVideoList);
        return resultPage;
    }

    /**
     * 将Video转换为UserVideoDto
     */
    private com.temimi.model.dto.UserVideoDto convertToUserVideoDto(Video video) {
        // 查询用户信息（使用安全版本，不抛异常）
        User user = userService.getUserByIdSafe(video.getUid());
        String uploaderName = user != null ? user.getNickname() : "未知UP主";
        String uploaderAvatar = user != null ? user.getAvatar() : null;

        // 查询统计信息
        VideoStats stats = videoStatsMapper.selectById(video.getVid());

        return com.temimi.model.dto.UserVideoDto.from(video, uploaderName, uploaderAvatar, stats);
    }

    @Override
    @Transactional
    public boolean deleteVideoCompletely(Integer vid, Integer uid) {
        // 1. 验证视频是否存在
        Video video = videoMapper.selectById(vid);
        if (video == null) {
            throw new BusinessException(BusinessErrorCode.VIDEO_NOT_FOUND);
        }

        // 2. 验证是否是视频作者
        if (!video.getUid().equals(uid)) {
            throw new BusinessException(BusinessErrorCode.PERMISSION_DENIED, "无权删除此视频");
        }

        try {
            // 3. 删除评论（包括所有子评论）
            QueryWrapper<com.temimi.model.entity.Comment> commentWrapper = new QueryWrapper<>();
            commentWrapper.eq("vid", vid);
            commentService.remove(commentWrapper);
            logger.info("删除视频 {} 的所有评论", vid);

            // 4. 删除弹幕
            QueryWrapper<com.temimi.model.entity.Danmu> danmuWrapper = new QueryWrapper<>();
            danmuWrapper.eq("vid", vid);
            danmuService.remove(danmuWrapper);
            logger.info("删除视频 {} 的所有弹幕", vid);

            // 5. 删除用户视频关联数据（点赞、投币、播放记录等）
            QueryWrapper<com.temimi.model.entity.UserVideo> userVideoWrapper = new QueryWrapper<>();
            userVideoWrapper.eq("vid", vid);
            userVideoService.remove(userVideoWrapper);
            logger.info("删除视频 {} 的用户关联数据", vid);

            // 6. 删除收藏夹关联数据
            QueryWrapper<com.temimi.model.entity.FavoriteVideo> favoriteVideoWrapper = new QueryWrapper<>();
            favoriteVideoWrapper.eq("vid", vid);
            favoriteVideoMapper.delete(favoriteVideoWrapper);
            logger.info("删除视频 {} 的收藏夹关联数据", vid);

            // 7. 删除视频统计数据
            videoStatsMapper.deleteById(vid);
            logger.info("删除视频 {} 的统计数据", vid);

            // 8. 删除视频文件（物理文件）
            deleteVideoFiles(video);

            // 9. 删除视频记录
            int result = videoMapper.deleteById(vid);
            logger.info("删除视频 {} 的数据库记录", vid);

            return result > 0;
        } catch (Exception e) {
            logger.error("删除视频 {} 失败: {}", vid, e.getMessage(), e);
            throw new BusinessException(BusinessErrorCode.VIDEO_DELETE_FAILED, "删除视频失败: " + e.getMessage());
        }
    }

    /**
     * 删除视频相关的物理文件
     */
    private void deleteVideoFiles(Video video) {
        try {
            // 删除视频文件
            if (video.getVideoUrl() != null && !video.getVideoUrl().isEmpty()) {
                String videoPath = uploadBasePath + video.getVideoUrl().replace("/", File.separator);
                File videoFile = new File(videoPath);
                if (videoFile.exists() && videoFile.delete()) {
                    logger.info("删除视频文件: {}", videoPath);
                } else {
                    logger.warn("视频文件不存在或删除失败: {}", videoPath);
                }
            }

            // 删除封面文件（如果不是默认封面）
            if (video.getCoverUrl() != null && !video.getCoverUrl().isEmpty() 
                && !video.getCoverUrl().equals(BusinessConstants.DEFAULT_COVER_PATH)) {
                String coverPath = uploadBasePath + video.getCoverUrl().replace("/", File.separator);
                File coverFile = new File(coverPath);
                if (coverFile.exists() && coverFile.delete()) {
                    logger.info("删除封面文件: {}", coverPath);
                } else {
                    logger.warn("封面文件不存在或删除失败: {}", coverPath);
                }
            }
        } catch (Exception e) {
            logger.error("删除视频文件时出错: {}", e.getMessage(), e);
            // 文件删除失败不影响数据库删除，只记录日志
        }
    }

    @Override
    public List<com.temimi.model.dto.UserVideoDto> getUserLoveVideos(Integer uid, Integer offset, Integer quantity) {
        // 查询用户点赞的视频记录
        QueryWrapper<com.temimi.model.entity.UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                   .eq("love", 1)
                   .orderByDesc("love_time")
                   .last("LIMIT " + offset + ", " + quantity);
        
        List<com.temimi.model.entity.UserVideo> userVideos = userVideoService.list(queryWrapper);
        
        // 获取视频详情
        return userVideos.stream().map(userVideo -> {
            Video video = videoMapper.selectById(userVideo.getVid());
            if (video == null) {
                return null;
            }
            
            // 获取视频作者信息
            User uploader = userService.getById(video.getUid());
            String uploaderName = uploader != null ? uploader.getNickname() : "未知用户";
            String uploaderAvatar = uploader != null ? uploader.getAvatar() : null;
            
            // 获取视频统计信息
            VideoStats stats = videoStatsMapper.selectById(video.getVid());
            
            return com.temimi.model.dto.UserVideoDto.from(video, uploaderName, uploaderAvatar, stats);
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }

    @Override
    public List<com.temimi.model.dto.UserVideoDto> getUserCoinVideos(Integer uid, Integer offset, Integer quantity) {
        // 查询用户投币的视频记录
        QueryWrapper<com.temimi.model.entity.UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                   .gt("coin", 0)
                   .orderByDesc("coin_time")
                   .last("LIMIT " + offset + ", " + quantity);
        
        List<com.temimi.model.entity.UserVideo> userVideos = userVideoService.list(queryWrapper);
        
        // 获取视频详情
        return userVideos.stream().map(userVideo -> {
            Video video = videoMapper.selectById(userVideo.getVid());
            if (video == null) {
                return null;
            }
            
            // 获取视频作者信息
            User uploader = userService.getById(video.getUid());
            String uploaderName = uploader != null ? uploader.getNickname() : "未知用户";
            String uploaderAvatar = uploader != null ? uploader.getAvatar() : null;
            
            // 获取视频统计信息
            VideoStats stats = videoStatsMapper.selectById(video.getVid());
            
            return com.temimi.model.dto.UserVideoDto.from(video, uploaderName, uploaderAvatar, stats);
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }

    @Override
    public List<com.temimi.model.dto.UserVideoDto> getUserHistory(Integer uid, Integer offset, Integer quantity) {
        // 查询用户观看过的视频记录（按播放时间倒序）
        QueryWrapper<com.temimi.model.entity.UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                   .gt("play", 0)  // 播放次数大于0
                   .isNotNull("play_time")  // 确保有播放时间
                   .orderByDesc("play_time")
                   .last("LIMIT " + offset + ", " + quantity);
        
        List<com.temimi.model.entity.UserVideo> userVideos = userVideoService.list(queryWrapper);
        
        // 获取视频详情
        return userVideos.stream().map(userVideo -> {
            Video video = videoMapper.selectById(userVideo.getVid());
            if (video == null) {
                return null;
            }
            
            // 获取视频作者信息
            User uploader = userService.getById(video.getUid());
            String uploaderName = uploader != null ? uploader.getNickname() : "未知用户";
            String uploaderAvatar = uploader != null ? uploader.getAvatar() : null;
            
            // 获取视频统计信息
            VideoStats stats = videoStatsMapper.selectById(video.getVid());
            
            // 传入播放时间
            return com.temimi.model.dto.UserVideoDto.from(video, uploaderName, uploaderAvatar, stats, userVideo.getPlayTime());
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }

    @Override
    public List<com.temimi.model.dto.UserVideoDto> getUserFavorites(Integer uid, Integer offset, Integer quantity) {
        // 查询用户收藏的视频（从 favorite_video 表）
        QueryWrapper<com.temimi.model.entity.FavoriteVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("fid", "SELECT fid FROM favorite WHERE uid = " + uid)
                   .isNull("is_remove")  // 未移除的收藏
                   .orderByDesc("time")
                   .last("LIMIT " + offset + ", " + quantity);
        
        // 需要先获取用户的所有收藏夹
        QueryWrapper<com.temimi.model.entity.Favorite> favQueryWrapper = new QueryWrapper<>();
        favQueryWrapper.eq("uid", uid).eq("is_delete", 0);
        List<com.temimi.model.entity.Favorite> favorites = favoriteService.list(favQueryWrapper);
        
        if (favorites.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        // 获取所有收藏夹的ID
        List<Integer> fids = favorites.stream().map(com.temimi.model.entity.Favorite::getFid).collect(Collectors.toList());
        
        // 查询这些收藏夹中的视频
        QueryWrapper<com.temimi.model.entity.FavoriteVideo> favVideoQueryWrapper = new QueryWrapper<>();
        favVideoQueryWrapper.in("fid", fids)
                           .isNull("is_remove")
                           .orderByDesc("time")
                           .last("LIMIT " + offset + ", " + quantity);
        
        List<com.temimi.model.entity.FavoriteVideo> favoriteVideos = favoriteVideoMapper.selectList(favVideoQueryWrapper);
        
        // 获取视频详情
        return favoriteVideos.stream().map(favoriteVideo -> {
            Video video = videoMapper.selectById(favoriteVideo.getVid());
            if (video == null) {
                return null;
            }
            
            // 获取视频作者信息
            User uploader = userService.getById(video.getUid());
            String uploaderName = uploader != null ? uploader.getNickname() : "未知用户";
            String uploaderAvatar = uploader != null ? uploader.getAvatar() : null;
            
            // 获取视频统计信息
            VideoStats stats = videoStatsMapper.selectById(video.getVid());
            
            return com.temimi.model.dto.UserVideoDto.from(video, uploaderName, uploaderAvatar, stats);
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }
}