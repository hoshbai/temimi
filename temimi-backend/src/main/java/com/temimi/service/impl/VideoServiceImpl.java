package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.constant.BusinessConstants;
import com.temimi.exception.BusinessException;
import com.temimi.exception.BusinessErrorCode;
import com.temimi.mapper.VideoMapper;
import com.temimi.mapper.VideoStatsMapper;
import com.temimi.model.entity.Video;
import com.temimi.model.entity.VideoStats;
import com.temimi.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoStatsMapper videoStatsMapper;
    
    @Value("${file.upload.path:D:/shiyou_upload/}")
    private String uploadBasePath;

    @Override
    public Page<Video> getVideoList(int pageNum, int pageSize) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", BusinessConstants.VIDEO_STATUS_APPROVED)
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
        return result > 0;
    }

    @Override
    public Video getVideoDetail(Integer vid) {
        Video video = videoMapper.selectById(vid);
        if (video == null) {
            throw new BusinessException(BusinessErrorCode.VIDEO_NOT_FOUND);
        }

        if (video.getStatus() == null || !video.getStatus().equals(BusinessConstants.VIDEO_STATUS_APPROVED)) {
            throw new BusinessException(BusinessErrorCode.VIDEO_STATUS_INVALID, "视频正在审核中或已被删除");
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
        video.setStatus(BusinessConstants.VIDEO_STATUS_PENDING);
        video.setUploadDate(LocalDateTime.now());
        
        return video;
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
        queryWrapper.eq("sc_id", scId)
                .eq("status", BusinessConstants.VIDEO_STATUS_APPROVED)
                .orderByDesc("upload_date");

        return videoMapper.selectPage(page, queryWrapper);
    }
}