package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.mapper.DynamicLikeMapper;
import com.temimi.mapper.DynamicMapper;
import com.temimi.model.entity.*;
import com.temimi.service.DynamicService;
import com.temimi.service.UserFollowService;
import com.temimi.service.UserService;
import com.temimi.service.UserVideoService;
import com.temimi.service.VideoService;
import com.temimi.service.VideoStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DynamicServiceImpl extends ServiceImpl<DynamicMapper, Dynamic> implements DynamicService {

    @Autowired
    private DynamicLikeMapper dynamicLikeMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserFollowService userFollowService;

    @Autowired
    private VideoStatsService videoStatsService;

    @Autowired
    private UserVideoService userVideoService;

    @Override
    @Transactional
    public Dynamic publish(Integer uid, Integer type, String content, String images, Integer vid) {
        Dynamic dynamic = new Dynamic();
        dynamic.setUid(uid);
        dynamic.setType(type);
        dynamic.setContent(content);
        dynamic.setImages(images);
        dynamic.setVid(vid);
        dynamic.setLove(0);
        dynamic.setCommentCount(0);
        dynamic.setForwardCount(0);
        dynamic.setStatus(0);
        dynamic.setCreateTime(LocalDateTime.now());
        dynamic.setUpdateTime(LocalDateTime.now());
        
        this.save(dynamic);
        return dynamic;
    }

    @Override
    public Page<Dynamic> getUserDynamics(Integer uid, Integer page, Integer size, Integer currentUid, Integer type) {
        QueryWrapper<Dynamic> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                   .eq("status", 0);
        
        // 如果指定了类型，则按类型筛选
        if (type != null) {
            queryWrapper.eq("type", type);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        Page<Dynamic> dynamicPage = new Page<>(page, size);
        Page<Dynamic> result = this.page(dynamicPage, queryWrapper);
        
        // 填充用户信息和视频信息
        fillDynamicInfo(result.getRecords(), currentUid);
        
        return result;
    }

    @Override
    public Page<Dynamic> getFollowingDynamics(Integer uid, Integer page, Integer size) {
        // 获取关注列表
        List<Integer> followingIds = userFollowService.getFollowingIds(uid);
        
        if (followingIds.isEmpty()) {
            return new Page<>(page, size);
        }
        
        QueryWrapper<Dynamic> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("uid", followingIds)
                   .eq("status", 0)
                   .orderByDesc("create_time");
        
        Page<Dynamic> dynamicPage = new Page<>(page, size);
        Page<Dynamic> result = this.page(dynamicPage, queryWrapper);
        
        // 填充用户信息和视频信息
        fillDynamicInfo(result.getRecords(), uid);
        
        return result;
    }

    @Override
    @Transactional
    public boolean toggleLike(Integer dynamicId, Integer uid) {
        QueryWrapper<DynamicLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dynamic_id", dynamicId).eq("uid", uid);
        
        DynamicLike existingLike = dynamicLikeMapper.selectOne(queryWrapper);
        
        Dynamic dynamic = this.getById(dynamicId);
        if (dynamic == null) {
            return false;
        }
        
        if (existingLike != null) {
            // 取消点赞
            dynamicLikeMapper.delete(queryWrapper);
            dynamic.setLove(Math.max(0, dynamic.getLove() - 1));
        } else {
            // 点赞
            DynamicLike like = new DynamicLike();
            like.setDynamicId(dynamicId);
            like.setUid(uid);
            like.setCreateTime(LocalDateTime.now());
            dynamicLikeMapper.insert(like);
            dynamic.setLove(dynamic.getLove() + 1);
        }
        
        this.updateById(dynamic);
        return existingLike == null; // 返回true表示点赞，false表示取消
    }

    @Override
    @Transactional
    public boolean deleteDynamic(Integer dynamicId, Integer uid) {
        Dynamic dynamic = this.getById(dynamicId);
        if (dynamic == null || !dynamic.getUid().equals(uid)) {
            return false;
        }
        
        dynamic.setStatus(2); // 标记为已删除
        return this.updateById(dynamic);
    }

    @Override
    public Dynamic getDetail(Integer dynamicId, Integer currentUid) {
        Dynamic dynamic = this.getById(dynamicId);
        if (dynamic == null || dynamic.getStatus() != 0) {
            return null;
        }
        
        fillDynamicInfo(List.of(dynamic), currentUid);
        return dynamic;
    }

    @Override
    @Transactional
    public Dynamic createVideoPublishDynamic(Integer uid, Integer vid) {
        // 检查是否已经为该视频创建过动态（避免重复）
        QueryWrapper<Dynamic> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                   .eq("vid", vid)
                   .eq("type", 5)  // 投稿视频类型
                   .ne("status", 2);  // 非删除状态
        
        Dynamic existing = this.getOne(queryWrapper);
        if (existing != null) {
            return existing;  // 已存在则直接返回
        }
        
        // 创建投稿视频动态
        Dynamic dynamic = new Dynamic();
        dynamic.setUid(uid);
        dynamic.setType(5);  // 5=投稿视频
        dynamic.setContent("投稿了视频");
        dynamic.setVid(vid);
        dynamic.setLove(0);
        dynamic.setCommentCount(0);
        dynamic.setForwardCount(0);
        dynamic.setStatus(0);
        dynamic.setCreateTime(LocalDateTime.now());
        dynamic.setUpdateTime(LocalDateTime.now());
        
        this.save(dynamic);
        return dynamic;
    }

    /**
     * 填充动态的用户信息和视频信息
     */
    private void fillDynamicInfo(List<Dynamic> dynamics, Integer currentUid) {
        for (Dynamic dynamic : dynamics) {
            // 填充用户信息
            User user = userService.getById(dynamic.getUid());
            if (user != null) {
                user.setPassword(null);
                dynamic.setUser(user);
            }
            
            // 填充视频信息（如果是转发视频或投稿视频类型）
            if (dynamic.getVid() != null && dynamic.getVid() > 0) {
                Video video = videoService.getById(dynamic.getVid());
                if (video != null) {
                    // 投稿视频动态需要填充视频统计数据
                    if (dynamic.getType() == 5) {
                        VideoStats stats = videoStatsService.getStatsByVid(video.getVid());
                        video.setStats(stats);
                        
                        // 检查当前用户是否点赞了视频
                        if (currentUid != null) {
                            UserVideo userVideo = userVideoService.getByUidAndVid(currentUid, video.getVid());
                            dynamic.setVideoLiked(userVideo != null && userVideo.getLove() != null && userVideo.getLove() == 1);
                        } else {
                            dynamic.setVideoLiked(false);
                        }
                    }
                    dynamic.setVideo(video);
                }
            }
            
            // 检查当前用户是否点赞
            if (currentUid != null) {
                QueryWrapper<DynamicLike> likeQuery = new QueryWrapper<>();
                likeQuery.eq("dynamic_id", dynamic.getId()).eq("uid", currentUid);
                dynamic.setIsLiked(dynamicLikeMapper.selectCount(likeQuery) > 0);
            } else {
                dynamic.setIsLiked(false);
            }
        }
    }
}
