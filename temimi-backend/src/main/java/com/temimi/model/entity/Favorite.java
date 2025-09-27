package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 收藏夹实体类
 * 对应数据库表: favorite
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("favorite")
public class Favorite {

    /**
     * 收藏夹ID
     */
    @TableId(value = "fid", type = IdType.AUTO)
    private Integer fid;

    /**
     * 所属用户ID
     */
    @TableField("uid")
    private Integer uid;

    /**
     * 收藏夹类型 1默认收藏夹 2用户创建
     */
    @TableField("type")
    private Integer type; // ✅ 修改: Byte -> Integer

    /**
     * 对外开放 0隐藏 1公开
     */
    @TableField("visible")
    private Integer visible; // ✅ 修改: Boolean -> Integer

    /**
     * 收藏夹封面
     */
    @TableField("cover")
    private String cover;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 简介
     */
    @TableField("description")
    private String description;

    /**
     * 收藏夹中视频数量
     */
    @TableField("count")
    private Integer count;

    /**
     * 是否删除 0否 1已删除
     */
    @TableField("is_delete")
    private Integer isDelete;
}