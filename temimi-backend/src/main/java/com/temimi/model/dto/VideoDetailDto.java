package com.temimi.model.dto;

import com.temimi.model.entity.Video;
import com.temimi.model.entity.VideoStats;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频详情DTO
 * 包含视频基本信息、UP主信息和统计信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoDetailDto {

    /**
     * 视频ID
     */
    private Integer vid;

    /**
     * UP主用户ID
     */
    private Integer uid;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 视频封面URL
     */
    private String coverUrl;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 视频时长（秒）
     */
    private Double duration;

    /**
     * 视频类型 1自制 2转载
     */
    private Integer type;

    /**
     * 作者声明 0不声明 1未经允许禁止转载
     */
    private Integer auth;

    /**
     * 主分区ID
     */
    private String mcId;

    /**
     * 子分区ID
     */
    private String scId;

    /**
     * 主分区名称
     */
    private String mcName;

    /**
     * 子分区名称
     */
    private String scName;

    /**
     * 标签
     */
    private String tags;

    /**
     * 简介
     */
    private String descr;

    /**
     * 状态 0审核中 1已过审 2未通过 3已删除
     */
    private Integer status;

    /**
     * 上传时间
     */
    private String uploadTime;

    /**
     * UP主昵称
     */
    private String uploaderName;

    /**
     * UP主头像URL
     */
    private String uploaderAvatar;

    /**
     * 播放量
     */
    private Integer playCount;

    /**
     * 弹幕数
     */
    private Integer danmuCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 投币数
     */
    private Integer coinCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 分享数
     */
    private Integer shareCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 从Video和VideoStats构建VideoDetailDto
     */
    public static VideoDetailDto from(Video video, String uploaderName, String uploaderAvatar, VideoStats stats) {
        return from(video, uploaderName, uploaderAvatar, stats, null, null);
    }

    /**
     * 从Video和VideoStats构建VideoDetailDto（包含分区名称）
     */
    public static VideoDetailDto from(Video video, String uploaderName, String uploaderAvatar, VideoStats stats, String mcName, String scName) {
        VideoDetailDto dto = new VideoDetailDto();
        dto.setVid(video.getVid());
        dto.setUid(video.getUid());
        dto.setTitle(video.getTitle());
        dto.setCoverUrl(video.getCoverUrl());
        dto.setVideoUrl(video.getVideoUrl());
        dto.setDuration(video.getDuration());
        dto.setType(video.getType());
        dto.setAuth(video.getAuth());
        dto.setMcId(video.getMcId());
        dto.setScId(video.getScId());
        dto.setMcName(mcName);
        dto.setScName(scName);
        dto.setTags(video.getTags());
        dto.setDescr(video.getDescr());
        dto.setStatus(video.getStatus());
        dto.setUploadTime(video.getUploadDate() != null ? video.getUploadDate().toString() : null);

        dto.setUploaderName(uploaderName);
        dto.setUploaderAvatar(uploaderAvatar);

        if (stats != null) {
            dto.setPlayCount(stats.getPlay());
            dto.setDanmuCount(stats.getDanmu());
            dto.setLikeCount(stats.getGood());
            dto.setCoinCount(stats.getCoin());
            dto.setCollectCount(stats.getCollect());
            dto.setShareCount(stats.getShare());
            dto.setCommentCount(stats.getComment());
        } else {
            dto.setPlayCount(0);
            dto.setDanmuCount(0);
            dto.setLikeCount(0);
            dto.setCoinCount(0);
            dto.setCollectCount(0);
            dto.setShareCount(0);
            dto.setCommentCount(0);
        }

        return dto;
    }
}
