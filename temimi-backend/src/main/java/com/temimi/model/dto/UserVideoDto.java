package com.temimi.model.dto;

import com.temimi.model.entity.Video;
import com.temimi.model.entity.VideoStats;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户视频DTO
 * 用于用户空间的视频列表展示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVideoDto {

    /**
     * 视频信息
     */
    private VideoInfo video;

    /**
     * 用户信息
     */
    private UserInfo user;

    /**
     * 统计信息
     */
    private StatsInfo stats;

    /**
     * 视频信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoInfo {
        private Integer vid;
        private String title;
        private String coverUrl;
        private String videoUrl;
        private Double duration;
        private Integer type;
        private Integer auth;
        private String mcId;
        private String scId;
        private String tags;
        private String descr;
        private Integer status;
        private String uploadDate;
        private String playTime;  // 播放时间（用于历史记录）
    }

    /**
     * 用户信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Integer uid;
        private String nickname;
        private String avatar;
    }

    /**
     * 统计信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatsInfo {
        private Integer play;
        private Integer danmu;
        private Integer good;
        private Integer coin;
        private Integer collect;
        private Integer share;
        private Integer comment;
    }

    /**
     * 从Video、用户信息和VideoStats构建UserVideoDto
     */
    public static UserVideoDto from(Video video, String uploaderName, String uploaderAvatar, VideoStats stats) {
        return from(video, uploaderName, uploaderAvatar, stats, null);
    }

    /**
     * 从Video、用户信息、VideoStats和播放时间构建UserVideoDto
     */
    public static UserVideoDto from(Video video, String uploaderName, String uploaderAvatar, VideoStats stats, java.time.LocalDateTime playTime) {
        UserVideoDto dto = new UserVideoDto();

        // 构建视频信息
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setVid(video.getVid());
        videoInfo.setTitle(video.getTitle());
        videoInfo.setCoverUrl(video.getCoverUrl());
        videoInfo.setVideoUrl(video.getVideoUrl());
        videoInfo.setDuration(video.getDuration());
        videoInfo.setType(video.getType());
        videoInfo.setAuth(video.getAuth());
        videoInfo.setMcId(video.getMcId());
        videoInfo.setScId(video.getScId());
        videoInfo.setTags(video.getTags());
        videoInfo.setDescr(video.getDescr());
        videoInfo.setStatus(video.getStatus());
        videoInfo.setUploadDate(video.getUploadDate() != null ? video.getUploadDate().toString() : null);
        videoInfo.setPlayTime(playTime != null ? playTime.toString() : null);
        dto.setVideo(videoInfo);

        // 构建用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setUid(video.getUid());
        userInfo.setNickname(uploaderName);
        userInfo.setAvatar(uploaderAvatar);
        dto.setUser(userInfo);

        // 构建统计信息
        StatsInfo statsInfo = new StatsInfo();
        if (stats != null) {
            statsInfo.setPlay(stats.getPlay());
            statsInfo.setDanmu(stats.getDanmu());
            statsInfo.setGood(stats.getGood());
            statsInfo.setCoin(stats.getCoin());
            statsInfo.setCollect(stats.getCollect());
            statsInfo.setShare(stats.getShare());
            statsInfo.setComment(stats.getComment());
        } else {
            statsInfo.setPlay(0);
            statsInfo.setDanmu(0);
            statsInfo.setGood(0);
            statsInfo.setCoin(0);
            statsInfo.setCollect(0);
            statsInfo.setShare(0);
            statsInfo.setComment(0);
        }
        dto.setStats(statsInfo);

        return dto;
    }
}
