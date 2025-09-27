package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 分区表实体类
 * 对应数据库表: category
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("category")
public class Category {

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