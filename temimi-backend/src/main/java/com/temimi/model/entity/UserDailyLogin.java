package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户每日登录记录表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_daily_login")
public class UserDailyLogin {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("uid")
    private Integer uid;

    @TableField("login_date")
    private LocalDate loginDate;

    @TableField("coin_rewarded")
    private Integer coinRewarded;

    @TableField("create_time")
    private LocalDateTime createTime;
}
