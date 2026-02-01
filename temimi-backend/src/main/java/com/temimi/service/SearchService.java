package com.temimi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 搜索服务接口
 */
public interface SearchService {
    
    /**
     * 搜索视频
     * @param keyword 搜索关键词
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 视频分页结果
     */
    Page<?> searchVideos(String keyword, Integer pageNum, Integer pageSize);
    
    /**
     * 搜索用户
     * @param keyword 搜索关键词
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 用户分页结果
     */
    Page<?> searchUsers(String keyword, Integer pageNum, Integer pageSize);
}
