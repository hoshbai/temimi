package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 弹幕表实体类
 * 对应数据库表: danmu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("danmu")
public class Danmu {

    /**
     * 弹幕ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 视频ID
     */
    @TableField("vid")
    private Integer vid;

    /**
     * 用户ID
     */
    @TableField("uid")
    private Integer uid;

    /**
     * 弹幕内容
     */
    @TableField("content")
    private String content;

    /**
     * 字体大小
     */
    @TableField("fontsize")
    private Integer fontsize; // ✅ 修改: Byte -> Integer

    /**
     * 弹幕模式 1滚动 2顶部 3底部
     */
    @TableField("mode")
    private Integer mode; // ✅ 修改: Byte -> Integer

    /**
     * 弹幕颜色 6位十六进制标准格式
     */
    @TableField("color")
    private String color;

    /**
     * 弹幕所在视频的时间点
     */
    @TableField("time_point")
    private Double timePoint;


    /**
     * 弹幕状态 1默认过审 2被举报审核中 3删除
     */
    @TableField("state")
    private Integer state; // ✅ 修改: Byte -> Integer

    /**
     * 发送弹幕的日期时间
     */
    @TableField("create_date")
    private LocalDateTime createDate;
}