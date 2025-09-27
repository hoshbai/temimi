package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 视频收藏夹关系表实体类
 * 对应数据库表: favorite_video
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("favorite_video")
public class FavoriteVideo {

    /**
     * 唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 视频ID
     */
    @TableField("vid")
    private Integer vid;

    /**
     * 收藏夹ID
     */
    @TableField("fid")
    private Integer fid;

    /**
     * 收藏时间
     */
    @TableField("time")
    private LocalDateTime time;

    /**
     * 是否移除 null否 1已移除
     */
    @TableField("is_remove")
    private Integer isRemove;
}