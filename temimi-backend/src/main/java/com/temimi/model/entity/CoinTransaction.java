package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 硬币交易记录表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("coin_transaction")
public class CoinTransaction {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("uid")
    private Integer uid;

    @TableField("amount")
    private Double amount;

    @TableField("type")
    private Integer type;

    @TableField("related_vid")
    private Integer relatedVid;

    @TableField("related_uid")
    private Integer relatedUid;

    @TableField("description")
    private String description;

    @TableField("create_time")
    private LocalDateTime createTime;
}
