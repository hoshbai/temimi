package com.temimi.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

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
}