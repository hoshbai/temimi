package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 视频数据统计表实体类
 * 对应数据库表: video_stats
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("video_stats")
public class VideoStats {

    /**
     * 视频ID
     */
    @TableId("vid")
    private Integer vid;

    /**
     * 播放量
     */
    @TableField("play")
    private Integer play; // 注意：这里使用 Long 以支持更大的播放量

    /**
     * 弹幕数
     */
    @TableField("danmu")
    private Integer danmu;

    /**
     * 点赞数
     */
    @TableField("good")
    private Integer good;

    /**
     * 点踩数
     */
    @TableField("bad")
    private Integer bad;

    /**
     * 投币数
     */
    @TableField("coin")
    private Integer coin;

    /**
     * 收藏数
     */
    @TableField("collect")
    private Integer collect;

    /**
     * 分享数
     */
    @TableField("share")
    private Integer share;

    /**
     * 评论数量统计
     */
    @TableField("comment")
    private Integer comment;
}