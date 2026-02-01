package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 用户表实体类
 * 对应数据库表: user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User {

    /**
     * 用户ID
     */
    @TableId(value = "uid", type = IdType.AUTO)
    private Integer uid;

    /**
     * 用户账号
     */
    @TableField("username")
    private String username;

    /**
     * 用户密码
     */
    @TableField("password")
    private String password;

    /**
     * 用户昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 用户头像url
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 主页背景图url
     */
    @TableField("background")
    private String background;

    /**
     * 性别 0女 1男 2未知
     */
    @TableField("gender")
    private Integer gender; // ✅ 已修改: Byte -> Integer

    /**
     * 个性签名
     */
    @TableField("description")
    private String description;

    /**
     * 经验值
     */
    @TableField("exp")
    private Integer exp;

    /**
     * 硬币数
     */
    @TableField("coin")
    private Double coin;

    /**
     * 获取格式化后的硬币数（保留一位小数）
     * 覆盖 Lombok 生成的 getter
     */
    public Double getCoin() {
        if (coin == null) {
            return 0.0;
        }
        // 使用 BigDecimal 精确计算，四舍五入保留一位小数
        BigDecimal bd = new BigDecimal(coin.toString());
        return bd.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 设置硬币数（自动格式化为一位小数）
     * 覆盖 Lombok 生成的 setter
     */
    public void setCoin(Double coin) {
        if (coin == null) {
            this.coin = 0.0;
        } else {
            // 使用 BigDecimal 精确计算，四舍五入保留一位小数
            BigDecimal bd = new BigDecimal(coin.toString());
            this.coin = bd.setScale(1, RoundingMode.HALF_UP).doubleValue();
        }
    }

    /**
     * 会员类型 0普通用户 1月度大会员 2季度大会员 3年度大会员
     */
    @TableField("vip")
    private Integer vip; // ✅ 已修改: Byte -> Integer

    /**
     * 状态 0正常 1封禁 2注销
     */
    @TableField("state")
    private Integer state; // ✅ 已修改: Byte -> Integer

    /**
     * 角色类型 0普通用户 1管理员 2超级管理员
     */
    @TableField("role")
    private Integer role; // ✅ 已修改: Byte -> Integer

    /**
     * 官方认证 0普通用户 1个人认证 2机构认证
     */
    @TableField("auth")
    private Integer auth; // ✅ 已修改: Byte -> Integer

    /**
     * 认证说明
     */
    @TableField("auth_msg")
    private String authMsg;

    /**
     * 创建时间
     */
    @TableField("create_date")
    private LocalDateTime createDate;

    /**
     * 注销时间
     */
    @TableField("delete_date")
    private LocalDateTime deleteDate;
    
    /**
     * 关注数（不对应数据库字段）
     */
    @TableField(exist = false)
    private Integer followsCount;
    
    /**
     * 粉丝数（不对应数据库字段）
     */
    @TableField(exist = false)
    private Integer fansCount;
    
    /**
     * 前端兼容字段：avatar_url
     * 不对应数据库字段，仅用于前端兼容
     */
    @TableField(exist = false)
    private String avatar_url;
    
    /**
     * 前端兼容字段：bg_url
     * 不对应数据库字段，仅用于前端兼容
     */
    @TableField(exist = false)
    private String bg_url;
    
    /**
     * 获取 avatar_url（映射到 avatar）
     */
    @JsonProperty("avatar_url")
    public String getAvatar_url() {
        return this.avatar;
    }
    
    /**
     * 设置 avatar_url（映射到 avatar）
     */
    public void setAvatar_url(String avatarUrl) {
        this.avatar = avatarUrl;
    }
    
    /**
     * 获取 bg_url（映射到 background）
     */
    @JsonProperty("bg_url")
    public String getBg_url() {
        return this.background;
    }
    
    /**
     * 设置 bg_url（映射到 background）
     */
    public void setBg_url(String bgUrl) {
        this.background = bgUrl;
    }
}