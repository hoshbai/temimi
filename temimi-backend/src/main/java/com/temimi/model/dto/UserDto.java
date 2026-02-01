package com.temimi.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.temimi.model.entity.User;

/**
 * 用户DTO（数据传输对象）
 * 用于在不同层之间传输用户数据
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDto extends User {
    
    /**
     * 是否已关注（用于前端显示）
     */
    private Boolean isFollowed;
    
    /**
     * 粉丝数量
     */
    private Integer fansCount;
    
    /**
     * 关注数量
     */
    private Integer followingCount;
    
    /**
     * 投稿视频数量
     */
    private Integer videoCount;
    
    /**
     * 获取总播放量
     */
    private Long totalViews;
    
    /**
     * 前端兼容：avatar_url 映射到 avatar
     * 继承自 User 类，无需重复定义
     */
}
