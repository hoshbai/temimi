package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 视频收藏里程碑记录表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("video_collect_milestone")
public class VideoCollectMilestone {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("vid")
    private Integer vid;

    @TableField("milestone")
    private Integer milestone;

    @TableField("rewarded")
    private Integer rewarded;

    @TableField("reward_time")
    private LocalDateTime rewardTime;
}
