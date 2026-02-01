package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.constant.BusinessConstants;
import com.temimi.mapper.UserMapper;
import com.temimi.mapper.VideoMapper;
import com.temimi.mapper.VideoStatsMapper;
import com.temimi.model.entity.User;
import com.temimi.model.entity.Video;
import com.temimi.model.entity.VideoStats;
import com.temimi.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoStatsMapper videoStatsMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<?> searchVideos(String keyword, Integer pageNum, Integer pageSize) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        
        // ✅ 解码关键词（前端已经 encodeURIComponent 编码了）
        String decodedKeyword = keyword;
        try {
            decodedKeyword = java.net.URLDecoder.decode(keyword, "UTF-8");
        } catch (Exception e) {
            // 如果解码失败，使用原始关键词
        }
        
        final String searchKeyword = decodedKeyword;  // lambda 需要 final 变量
        
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", BusinessConstants.VIDEO_STATUS_APPROVED)  // 只搜索已审核通过的视频
                .and(wrapper -> wrapper
                    .like("title", searchKeyword)  // 标题包含关键词
                    .or()
                    .like("descr", searchKeyword)  // 或简介包含关键词
                    .or()
                    .like("tags", searchKeyword)   // 或标签包含关键词
                )
                .orderByDesc("upload_date");  // 按上传时间倒序
        
        Page<Video> videoPage = videoMapper.selectPage(page, queryWrapper);
        
        // 转换为前端需要的格式
        List<Map<String, Object>> result = videoPage.getRecords().stream().map(video -> {
            Map<String, Object> item = new HashMap<>();
            
            // 视频信息
            Map<String, Object> videoInfo = new HashMap<>();
            videoInfo.put("vid", video.getVid());
            videoInfo.put("title", video.getTitle());
            videoInfo.put("coverUrl", video.getCoverUrl());
            videoInfo.put("duration", video.getDuration());
            videoInfo.put("uploadDate", video.getUploadDate());
            videoInfo.put("descr", video.getDescr());
            item.put("video", videoInfo);
            
            // 统计信息
            VideoStats stats = videoStatsMapper.selectById(video.getVid());
            Map<String, Object> statsInfo = new HashMap<>();
            if (stats != null) {
                statsInfo.put("play", stats.getPlay());
                statsInfo.put("danmu", stats.getDanmu());
                statsInfo.put("good", stats.getGood());
                statsInfo.put("collect", stats.getCollect());
            } else {
                statsInfo.put("play", 0);
                statsInfo.put("danmu", 0);
                statsInfo.put("good", 0);
                statsInfo.put("collect", 0);
            }
            item.put("stats", statsInfo);
            
            // UP主信息
            User user = userMapper.selectById(video.getUid());
            Map<String, Object> userInfo = new HashMap<>();
            if (user != null) {
                userInfo.put("uid", user.getUid());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatar", user.getAvatar());
            }
            item.put("user", userInfo);
            
            return item;
        }).collect(Collectors.toList());
        
        Page<Map<String, Object>> resultPage = new Page<>(pageNum, pageSize, videoPage.getTotal());
        resultPage.setRecords(result);
        
        return resultPage;
    }

    @Override
    public Page<?> searchUsers(String keyword, Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        
        // ✅ 解码关键词（前端已经 encodeURIComponent 编码了）
        String decodedKeyword = keyword;
        try {
            decodedKeyword = java.net.URLDecoder.decode(keyword, "UTF-8");
        } catch (Exception e) {
            // 如果解码失败，使用原始关键词
        }
        
        final String searchKeyword = decodedKeyword;  // lambda 需要 final 变量
        
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("state", BusinessConstants.USER_STATE_NORMAL)  // 只搜索正常状态的用户
                .ne("state", BusinessConstants.USER_STATE_DELETED)  // 排除已注销的用户
                .and(wrapper -> wrapper
                    .like("nickname", searchKeyword)  // 昵称包含关键词
                    .or()
                    .like("username", searchKeyword)  // 或用户名包含关键词
                    .or()
                    .like("description", searchKeyword)  // 或简介包含关键词
                )
                .orderByDesc("exp");  // 按经验值倒序（活跃度）
        
        Page<User> userPage = userMapper.selectPage(page, queryWrapper);
        
        // 转换为前端需要的格式
        List<Map<String, Object>> result = userPage.getRecords().stream().map(user -> {
            Map<String, Object> item = new HashMap<>();
            item.put("uid", user.getUid());
            item.put("username", user.getUsername());
            item.put("nickname", user.getNickname());
            item.put("avatar", user.getAvatar());
            item.put("description", user.getDescription());
            item.put("gender", user.getGender());
            item.put("exp", user.getExp());
            item.put("vip", user.getVip());
            item.put("auth", user.getAuth());
            
            // 统计用户的视频数量
            QueryWrapper<Video> videoQuery = new QueryWrapper<>();
            videoQuery.eq("uid", user.getUid())
                      .eq("status", BusinessConstants.VIDEO_STATUS_APPROVED);
            long videoCount = videoMapper.selectCount(videoQuery);
            item.put("videoCount", videoCount);
            
            // TODO: 添加粉丝数和关注数统计（需要 user_follow 表）
            item.put("fansCount", 0);
            item.put("followsCount", 0);
            return item;
        }).collect(Collectors.toList());
        
        Page<Map<String, Object>> resultPage = new Page<>(pageNum, pageSize, userPage.getTotal());
        resultPage.setRecords(result);
        
        return resultPage;
    }
}
