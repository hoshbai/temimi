package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.constant.BusinessConstants;
import com.temimi.exception.BusinessException;
import com.temimi.exception.BusinessErrorCode;
import com.temimi.mapper.UserVideoMapper;
import com.temimi.model.entity.UserVideo;
import com.temimi.service.UserVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserVideoServiceImpl extends ServiceImpl<UserVideoMapper, UserVideo> implements UserVideoService {

    @Autowired
    private UserVideoMapper userVideoMapper;

    @Override
    @Transactional
    public boolean recordPlay(Integer uid, Integer vid) {
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("vid", vid);
        UserVideo userVideo = userVideoMapper.selectOne(queryWrapper);

        if (userVideo == null) {
            userVideo = createNewUserVideoRecord(uid, vid);
            userVideo.setPlay(1);
            userVideo.setPlayTime(LocalDateTime.now());
            return userVideoMapper.insert(userVideo) > 0;
        } else {
            userVideo.setPlay(userVideo.getPlay() + 1);
            userVideo.setPlayTime(LocalDateTime.now());
            return userVideoMapper.updateById(userVideo) > 0;
        }
    }

    @Override
    @Transactional
    public boolean likeVideo(Integer uid, Integer vid) {
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("vid", vid);
        UserVideo userVideo = userVideoMapper.selectOne(queryWrapper);

        if (userVideo == null) {
            userVideo = createNewUserVideoRecord(uid, vid);
            userVideo.setLove(BusinessConstants.USER_ACTION_ACTIVE);
            userVideo.setLoveTime(LocalDateTime.now());
            return userVideoMapper.insert(userVideo) > 0;
        } else {
            if (userVideo.getLove() == BusinessConstants.USER_ACTION_INACTIVE) {
                userVideo.setLove(BusinessConstants.USER_ACTION_ACTIVE);
                userVideo.setLoveTime(LocalDateTime.now());
                return userVideoMapper.updateById(userVideo) > 0;
            } else {
                return true; // 已点赞，直接返回成功
            }
        }
    }

    @Override
    @Transactional
    public boolean unlikeVideo(Integer uid, Integer vid) {
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("vid", vid);
        UserVideo userVideo = userVideoMapper.selectOne(queryWrapper);

        if (userVideo != null && userVideo.getLove() == BusinessConstants.USER_ACTION_ACTIVE) {
            userVideo.setLove(BusinessConstants.USER_ACTION_INACTIVE);
            return userVideoMapper.updateById(userVideo) > 0;
        }
        return true; // 未点赞或记录不存在，直接返回成功
    }

    @Override
    @Transactional
    public boolean coinVideo(Integer uid, Integer vid, Integer coinCount) {
        if (coinCount < BusinessConstants.COIN_MIN_COUNT || coinCount > BusinessConstants.COIN_MAX_COUNT) {
            throw new BusinessException(BusinessErrorCode.COIN_COUNT_INVALID, "投币数量只能是" + BusinessConstants.COIN_MIN_COUNT + "或" + BusinessConstants.COIN_MAX_COUNT);
        }

        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("vid", vid);
        UserVideo userVideo = userVideoMapper.selectOne(queryWrapper);

        if (userVideo == null) {
            userVideo = createNewUserVideoRecord(uid, vid);
            userVideo.setCoin(coinCount);
            userVideo.setCoinTime(LocalDateTime.now());
            return userVideoMapper.insert(userVideo) > 0;
        } else {
            userVideo.setCoin(coinCount);
            userVideo.setCoinTime(LocalDateTime.now());
            return userVideoMapper.updateById(userVideo) > 0;
        }
    }

    @Override
    @Transactional
    public boolean collectVideo(Integer uid, Integer vid) {
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("vid", vid);
        UserVideo userVideo = userVideoMapper.selectOne(queryWrapper);

        if (userVideo == null) {
            userVideo = createNewUserVideoRecord(uid, vid);
            userVideo.setCollect(BusinessConstants.USER_ACTION_ACTIVE);
            return userVideoMapper.insert(userVideo) > 0;
        } else {
            if (userVideo.getCollect() == BusinessConstants.USER_ACTION_INACTIVE) {
                userVideo.setCollect(BusinessConstants.USER_ACTION_ACTIVE);
                return userVideoMapper.updateById(userVideo) > 0;
            } else {
                return true; // 已收藏，直接返回成功
            }
        }
    }

    @Override
    @Transactional
    public boolean uncollectVideo(Integer uid, Integer vid) {
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("vid", vid);
        UserVideo userVideo = userVideoMapper.selectOne(queryWrapper);

        if (userVideo != null && userVideo.getCollect() == BusinessConstants.USER_ACTION_ACTIVE) {
            userVideo.setCollect(BusinessConstants.USER_ACTION_INACTIVE);
            return userVideoMapper.updateById(userVideo) > 0;
        }
        return true; // 未收藏或记录不存在，直接返回成功
    }

    /**
     * 创建新的用户视频记录，设置默认值
     */
    private UserVideo createNewUserVideoRecord(Integer uid, Integer vid) {
        UserVideo userVideo = new UserVideo();
        userVideo.setUid(uid);
        userVideo.setVid(vid);
        userVideo.setPlay(BusinessConstants.STATS_INITIAL_VALUE);
        userVideo.setLove(BusinessConstants.USER_ACTION_INACTIVE);
        userVideo.setCoin(BusinessConstants.STATS_INITIAL_VALUE);
        userVideo.setCollect(BusinessConstants.USER_ACTION_INACTIVE);
        return userVideo;
    }
}