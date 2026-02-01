package com.temimi.service;

import com.temimi.model.dto.VideoDetailDto;
import com.temimi.model.entity.Video;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 视频服务接口
 */
public interface VideoService extends IService<Video> {

    /**
     * 分页查询视频列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<Video> getVideoList(int pageNum, int pageSize);

    /**
     * 根据视频ID获取视频详情
     * @param vid 视频ID
     * @return 视频详情
     */
    Video getVideoDetail(Integer vid);

    /**
     * 用户投稿视频 (旧方法，可保留或废弃)
     * @param video 视频对象
     * @param uid 投稿用户ID
     * @return 投稿是否成功
     */
    boolean uploadVideo(Video video, Integer uid);

    /**
     * 用户上传视频文件 (新方法，核心实现)
     * @param videoFile 视频文件
     * @param coverFile 封面文件 (可选)
     * @param title 视频标题
     * @param scId 子分区ID
     * @param tags 标签
     * @param descr 简介
     * @param uid 投稿用户ID
     * @return 视频的URL
     * @throws IOException 文件处理异常
     */
    @Transactional
    String uploadVideo(MultipartFile videoFile, MultipartFile coverFile, String title, String scId, String tags, String descr, Integer uid) throws IOException;

    /**
     * 根据子分区ID分页获取视频
     * @param scId 子分区ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<Video> getVideosByCategoryId(String scId, int pageNum, int pageSize);

    /**
     * 根据状态分页获取视频（用于管理员审核）
     * @param status 视频状态
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<Video> getVideosByStatus(Integer status, int pageNum, int pageSize);

    /**
     * 更新视频状态
     * @param vid 视频ID
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateVideoStatus(Integer vid, Integer status);

    /**
     * 搜索视频
     * @param keyword 搜索关键词
     * @return 视频列表
     */
    List<Video> searchVideos(String keyword);

    /**
     * 分页查询视频列表（包含用户和统计信息）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<VideoDetailDto> getVideoListWithDetails(int pageNum, int pageSize);

    /**
     * 根据视频ID获取视频详情（包含用户和统计信息）
     * @param vid 视频ID
     * @return 视频详情DTO
     */
    VideoDetailDto getVideoDetailWithInfo(Integer vid);

    /**
     * 根据子分区ID分页获取视频（包含用户和统计信息）
     * @param scId 子分区ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<VideoDetailDto> getVideosByCategoryWithDetails(String scId, int pageNum, int pageSize);

    /**
     * 根据用户ID分页获取用户投稿的视频（包含统计信息）
     * @param uid 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<VideoDetailDto> getVideosByUserWithDetails(Integer uid, int pageNum, int pageSize);

    /**
     * 增加视频播放量
     * @param vid 视频ID
     */
    void incrementPlayCount(Integer vid);

    /**
     * 获取用户投稿视频列表（包含用户和统计信息，支持排序）
     * @param uid 用户ID
     * @param rule 排序规则：1-最新发布, 2-最多播放, 3-最多点赞
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<com.temimi.model.dto.UserVideoDto> getUserWorksWithDetails(Integer uid, Integer rule, int pageNum, int pageSize);

    /**
     * 删除视频及其相关数据（级联删除）
     * @param vid 视频ID
     * @param uid 用户ID（用于权限验证）
     * @return 是否删除成功
     */
    @Transactional
    boolean deleteVideoCompletely(Integer vid, Integer uid);

    /**
     * 获取用户点赞的视频列表
     * @param uid 用户ID
     * @param offset 偏移量
     * @param quantity 数量
     * @return 视频列表
     */
    List<com.temimi.model.dto.UserVideoDto> getUserLoveVideos(Integer uid, Integer offset, Integer quantity);

    /**
     * 获取用户投币的视频列表
     * @param uid 用户ID
     * @param offset 偏移量
     * @param quantity 数量
     * @return 视频列表
     */
    List<com.temimi.model.dto.UserVideoDto> getUserCoinVideos(Integer uid, Integer offset, Integer quantity);

    /**
     * 获取用户观看历史记录
     * @param uid 用户ID
     * @param offset 偏移量
     * @param quantity 数量
     * @return 视频列表
     */
    List<com.temimi.model.dto.UserVideoDto> getUserHistory(Integer uid, Integer offset, Integer quantity);

    /**
     * 获取用户收藏的视频列表（所有收藏夹）
     * @param uid 用户ID
     * @param offset 偏移量
     * @param quantity 数量
     * @return 视频列表
     */
    List<com.temimi.model.dto.UserVideoDto> getUserFavorites(Integer uid, Integer offset, Integer quantity);

}