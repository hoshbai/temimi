package com.temimi.service.impl;

import com.temimi.model.entity.User;
import com.temimi.model.entity.Video;
import com.temimi.service.SearchService;
import com.temimi.service.UserService;
import com.temimi.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {
    
    @Autowired
    private VideoService videoService;
    
    @Autowired
    private UserService userService;
    
    @Override
    public List<Video> searchVideos(String keyword, int pageNum, int pageSize) {
        return videoService.searchVideos(keyword);
    }
    
    @Override
    public List<User> searchUsers(String keyword, int pageNum, int pageSize) {
        return userService.searchUsers(keyword);
    }
    
    @Override
    public SearchResult searchAll(String keyword) {
        List<Video> videos = searchVideos(keyword, 1, 10);
        List<User> users = searchUsers(keyword, 1, 10);
        return new SearchResult(videos, users);
    }
}