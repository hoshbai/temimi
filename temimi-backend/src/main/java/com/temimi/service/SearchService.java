package com.temimi.service;

import com.temimi.model.entity.User;
import com.temimi.model.entity.Video;
import java.util.List;

/**
 * 搜索服务接口
 */
public interface SearchService {
    
    /**
     * 搜索视频
     * @param keyword 搜索关键词
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 视频列表
     */
    List<Video> searchVideos(String keyword, int pageNum, int pageSize);
    
    /**
     * 搜索用户
     * @param keyword 搜索关键词
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 用户列表
     */
    List<User> searchUsers(String keyword, int pageNum, int pageSize);
    
    /**
     * 综合搜索
     * @param keyword 搜索关键词
     * @return 搜索结果（包含视频和用户）
     */
    SearchResult searchAll(String keyword);
    
    /**
     * 搜索结果封装类
     */
    class SearchResult {
        private List<Video> videos;
        private List<User> users;
        
        // 构造函数、getter、setter
        public SearchResult(List<Video> videos, List<User> users) {
            this.videos = videos;
            this.users = users;
        }
        
        public List<Video> getVideos() { return videos; }
        public void setVideos(List<Video> videos) { this.videos = videos; }
        public List<User> getUsers() { return users; }
        public void setUsers(List<User> users) { this.users = users; }
    }
}