package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 分区表实体类
 * 对应数据库表: category
 * 注意：此表使用联合主键(mc_id, sc_id)，MyBatis-Plus 不支持联合主键，因此不使用 @TableId
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("category")
public class Category {

    /**
     * 主分区ID (联合主键之一)
     */
    @TableField("mc_id")
    private String mcId;

    /**
     * 子分区ID (联合主键之一)
     */
    @TableField("sc_id")
    private String scId;

    /**
     * 主分区名称
     */
    @TableField("mc_name")
    private String mcName;

    /**
     * 子分区名称
     */
    @TableField("sc_name")
    private String scName;

    /**
     * 描述
     */
    @TableField("descr")
    private String descr;

    /**
     * 推荐标签
     */
    @TableField("rcm_tag")
    private String rcmTag;
}