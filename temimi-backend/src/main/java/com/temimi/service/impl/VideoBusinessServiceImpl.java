package com.temimi.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.exception.BusinessException;
import com.temimi.exception.BusinessErrorCode;
import com.temimi.model.entity.Video;
import com.temimi.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 视频业务聚合服务实现 - 遵循Service层聚合原则和Transaction Management Layer原则
 */
@Service
public class VideoBusinessServiceImpl implements VideoBusinessService {
    
    @Autowired
    private VideoService videoService;
    
    @Autowired
    private VideoStatsService videoStatsService;
    
    @Autowired
    private UserVideoService userVideoService;
    
    @Autowired
    private FavoriteService favoriteService;
    
    // === 查询操作 ===
    @Override
    public Page<Video> getVideoList(int pageNum, int pageSize) {
        return videoService.getVideoList(pageNum, pageSize);
    }
    
    @Override
    public Video getVideoDetail(Integer vid) {
        return videoService.getVideoDetail(vid);
    }
    
    @Override
    public Page<Video> getVideosByCategory(String scId, int pageNum, int pageSize) {
        return videoService.getVideosByCategoryId(scId, pageNum, pageSize);
    }
    
    @Override
    public List<Video> searchVideos(String keyword) {
        return videoService.searchVideos(keyword);
    }
    
    // === 视频管理操作 ===
    @Override
    @Transactional
    public String uploadVideo(MultipartFile videoFile, MultipartFile coverFile, 
                             String title, String scId, String tags, String descr, Integer uid) {
        try {
            return videoService.uploadVideo(videoFile, coverFile, title, scId, tags, descr, uid);
        } catch (Exception e) {
            throw new BusinessException(BusinessErrorCode.VIDEO_UPLOAD_FAILED, "视频上传失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateVideoStatus(Integer vid, Integer status) {
        return videoService.updateVideoStatus(vid, status);
    }
    
    // === 用户交互操作（聚合用户行为 + 统计更新） ===
    @Override
    @Transactional
    public boolean likeVideo(Integer uid, Integer vid) {
        boolean userSuccess = userVideoService.likeVideo(uid, vid);
        if (userSuccess) {
            videoStatsService.incrementLikeCount(vid);
        }
        return userSuccess;
    }
    
    @Override
    @Transactional
    public boolean unlikeVideo(Integer uid, Integer vid) {
        boolean userSuccess = userVideoService.unlikeVideo(uid, vid);
        if (userSuccess) {
            videoStatsService.decrementLikeCount(vid);
        }
        return userSuccess;
    }
    
    @Override
    @Transactional
    public boolean coinVideo(Integer uid, Integer vid, Integer count) {
        boolean userSuccess = userVideoService.coinVideo(uid, vid, count);
        if (userSuccess) {
            // 投币数量可能是1或2，需要根据实际投币数更新统计
            for (int i = 0; i < count; i++) {
                videoStatsService.incrementCoinCount(vid);
            }
        }
        return userSuccess;
    }
    
    @Override
    @Transactional
    public boolean collectVideo(Integer uid, Integer vid, Integer fid) {
        // 1. 先记录用户收藏行为
        boolean userSuccess = userVideoService.collectVideo(uid, vid);
        
        // 2. 将视频添加到指定收藏夹
        if (userSuccess) {
            favoriteService.addVideoToFavorite(vid, fid, uid);
            videoStatsService.incrementCollectCount(vid);
        }
        
        return userSuccess;
    }
    
    @Override
    @Transactional
    public boolean uncollectVideo(Integer uid, Integer vid) {
        boolean userSuccess = userVideoService.uncollectVideo(uid, vid);
        if (userSuccess) {
            videoStatsService.decrementCollectCount(vid);
            // 注意：这里可能需要从所有收藏夹中移除，但需要重新设计接口来处理
            // 目前的设计缺陷：不知道要从哪个收藏夹移除
        }
        return userSuccess;
    }
    
    @Override
    @Transactional
    public boolean recordPlay(Integer uid, Integer vid) {
        boolean userSuccess = userVideoService.recordPlay(uid, vid);
        if (userSuccess) {
            videoStatsService.incrementPlayCount(vid);
        }
        return userSuccess;
    }
}