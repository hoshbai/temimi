package com.temimi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.temimi.model.entity.Dynamic;

import java.util.List;

public interface DynamicService extends IService<Dynamic> {
    
    /**
     * 发布动态
     */
    Dynamic publish(Integer uid, Integer type, String content, String images, Integer vid);
    
    /**
     * 获取用户动态列表
     * @param type 动态类型筛选（可选，5=视频）
     */
    Page<Dynamic> getUserDynamics(Integer uid, Integer page, Integer size, Integer currentUid, Integer type);
    
    /**
     * 获取关注用户的动态列表（动态流）
     */
    Page<Dynamic> getFollowingDynamics(Integer uid, Integer page, Integer size);
    
    /**
     * 点赞/取消点赞
     */
    boolean toggleLike(Integer dynamicId, Integer uid);
    
    /**
     * 删除动态
     */
    boolean deleteDynamic(Integer dynamicId, Integer uid);
    
    /**
     * 获取动态详情
     */
    Dynamic getDetail(Integer dynamicId, Integer currentUid);
    
    /**
     * 为投稿视频创建动态（视频审核通过时调用）
     */
    Dynamic createVideoPublishDynamic(Integer uid, Integer vid);
}
