package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户视频关联表实体类
 * 对应数据库表: user_video
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_video")
public class UserVideo {

    /**
     * 唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 观看视频的用户UID
     */
    @TableField("uid")
    private Integer uid;

    /**
     * 视频ID
     */
    @TableField("vid")
    private Integer vid;

    /**
     * 播放次数
     */
    @TableField("play")
    private Integer play;

    /**
     * 点赞 0没赞 1已点赞
     */
    @TableField("love")
    private Integer love;

    /**
     * 不喜欢 0没点 1已不喜欢
     */
    @TableField("unlove")
    private Integer unlove;

    /**
     * 投币数 0-2 默认0
     */
    @TableField("coin")
    private Integer coin;

    /**
     * 收藏 0没收藏 1已收藏
     */
    @TableField("collect")
    private Integer collect;

    /**
     * 最近播放时间
     */
    @TableField("play_time")
    private LocalDateTime playTime;

    /**
     * 点赞时间
     */
    @TableField("love_time")
    private LocalDateTime loveTime;

    /**
     * 投币时间
     */
    @TableField("coin_time")
    private LocalDateTime coinTime;
}