package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 动态点赞表实体类
 * 对应数据库表: dynamic_like
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("dynamic_like")
public class DynamicLike {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 动态ID
     */
    @TableField("dynamic_id")
    private Integer dynamicId;

    /**
     * 用户ID
     */
    @TableField("uid")
    private Integer uid;

    /**
     * 点赞时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
