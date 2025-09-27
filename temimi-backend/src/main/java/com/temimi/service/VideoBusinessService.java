package com.temimi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.entity.Video;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 视频业务聚合服务 - 统一管理视频相关的所有业务逻辑
 */
public interface VideoBusinessService {
    
    // === 查询操作 ===
    Page<Video> getVideoList(int pageNum, int pageSize);
    Video getVideoDetail(Integer vid);
    Page<Video> getVideosByCategory(String scId, int pageNum, int pageSize);
    List<Video> searchVideos(String keyword);
    
    // === 视频管理操作 ===
    String uploadVideo(MultipartFile videoFile, MultipartFile coverFile, 
                      String title, String scId, String tags, String descr, Integer uid);
    boolean updateVideoStatus(Integer vid, Integer status);
    
    // === 用户交互操作（聚合用户行为 + 统计更新） ===
    boolean likeVideo(Integer uid, Integer vid);
    boolean unlikeVideo(Integer uid, Integer vid);
    boolean coinVideo(Integer uid, Integer vid, Integer count);
    boolean collectVideo(Integer uid, Integer vid, Integer fid);
    boolean uncollectVideo(Integer uid, Integer vid);
    boolean recordPlay(Integer uid, Integer vid);
}