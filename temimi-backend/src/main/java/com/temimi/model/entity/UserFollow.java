package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户关注关系实体类
 * 对应数据库表: user_follow
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_follow")
public class UserFollow {

    /**
     * 唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 关注者UID (谁关注)
     */
    @TableField("follower_id")
    private Integer followerId;

    /**
     * 被关注者UID (关注谁)
     */
    @TableField("following_id")
    private Integer followingId;

    /**
     * 关注时间
     */
    @TableField("follow_time")
    private LocalDateTime followTime;

    /**
     * 是否互相关注 0否 1是
     */
    @TableField("is_mutual")
    private Integer isMutual;

    /**
     * 关注状态 0已取消 1已关注
     */
    @TableField("status")
    private Integer status;
}
