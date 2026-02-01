package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 视频表实体类
 * 对应数据库表: video
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("video")
public class Video {

    /**
     * 视频ID
     */
    @TableId(value = "vid", type = IdType.AUTO)
    private Integer vid;

    /**
     * 投稿用户ID
     */
    @TableField("uid")
    private Integer uid;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 类型 1自制 2转载
     */
    @TableField("type")
    private Integer type; // ✅ 已修改: Byte -> Integer

    /**
     * 作者声明 0不声明 1未经允许禁止转载
     */
    @TableField("auth")
    private Integer auth; // ✅ 已修改: Byte -> Integer

    /**
     * 播放总时长 单位秒
     */
    @TableField("duration")
    private Double duration;

    /**
     * 主分区ID
     */
    @TableField("mc_id")
    private String mcId;

    /**
     * 子分区ID
     */
    @TableField("sc_id")
    private String scId;

    /**
     * 标签 回车分隔
     */
    @TableField("tags")
    private String tags;

    /**
     * 简介
     */
    @TableField("descr")
    private String descr;

    /**
     * 封面url
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 视频url
     */
    @TableField("video_url")
    private String videoUrl;

    /**
     * 状态 0审核中 1已过审 2未通过 3已删除
     */
    @TableField("status")
    private Integer status; // ✅ 已修改: Byte -> Integer

    /**
     * 上传时间
     */
    @TableField("upload_date")
    private LocalDateTime uploadDate;

    /**
     * 删除时间
     */
    @TableField("delete_date")
    private LocalDateTime deleteDate;

    // ==================== 非数据库字段 ====================

    /**
     * 视频统计数据（非数据库字段）
     */
    @TableField(exist = false)
    private VideoStats stats;
}