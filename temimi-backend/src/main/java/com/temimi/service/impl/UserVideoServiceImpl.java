package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.constant.BusinessConstants;
import com.temimi.exception.BusinessException;
import com.temimi.exception.BusinessErrorCode;
import com.temimi.mapper.UserVideoMapper;
import com.temimi.model.entity.UserVideo;
import com.temimi.service.CoinService;
import com.temimi.service.UserVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserVideoServiceImpl extends ServiceImpl<UserVideoMapper, UserVideo> implements UserVideoService {

    @Autowired
    private UserVideoMapper userVideoMapper;

    @Autowired
    private CoinService coinService;

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
        // ✅ 使用幂等 SQL：INSERT ... ON DUPLICATE KEY UPDATE
        // 避免先查询后插入的并发问题
        int result = userVideoMapper.likeVideoIdempotent(uid, vid);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean unlikeVideo(Integer uid, Integer vid) {
        // 直接更新即可（幂等操作）
        int result = userVideoMapper.unlikeVideo(uid, vid);
        return result > 0; // 即使记录不存在也返回成功
    }

    @Override
    @Transactional
    public boolean coinVideo(Integer uid, Integer vid, Integer coinCount) {
        if (coinCount < BusinessConstants.COIN_MIN_COUNT || coinCount > BusinessConstants.COIN_MAX_COUNT) {
            throw new BusinessException(BusinessErrorCode.COIN_COUNT_INVALID, "投币数量只能是" + BusinessConstants.COIN_MIN_COUNT + "或" + BusinessConstants.COIN_MAX_COUNT);
        }

        // ✅ 检查用户已投币数量
        LambdaQueryWrapper<UserVideo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserVideo::getUid, uid).eq(UserVideo::getVid, vid);
        UserVideo userVideo = userVideoMapper.selectOne(wrapper);
        
        int alreadyCoined = 0;
        if (userVideo != null && userVideo.getCoin() != null) {
            alreadyCoined = userVideo.getCoin();
        }
        
        // ✅ 检查是否超过最大投币数
        if (alreadyCoined + coinCount > BusinessConstants.COIN_MAX_COUNT) {
            throw new BusinessException(BusinessErrorCode.COIN_LIMIT_EXCEEDED, 
                "每个视频最多投" + BusinessConstants.COIN_MAX_COUNT + "个币，您已投" + alreadyCoined + "个币");
        }

        // 处理硬币交易和奖励
        coinService.coinToVideo(uid, vid, coinCount);

        // ✅ 使用累加而不是覆盖
        int result = userVideoMapper.coinVideoIncrement(uid, vid, coinCount);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean collectVideo(Integer uid, Integer vid) {
        // ✅ 使用幂等 SQL
        int result = userVideoMapper.collectVideoIdempotent(uid, vid);
        
        // 检查收藏里程碑奖励
        if (result > 0) {
            coinService.checkCollectMilestone(vid);
        }
        
        return result > 0;
    }

    @Override
    @Transactional
    public boolean uncollectVideo(Integer uid, Integer vid) {
        // 直接更新即可（幂等操作）
        int result = userVideoMapper.uncollectVideo(uid, vid);
        return result > 0;
    }

    @Override
    public boolean shouldIncrementPlayCount(Integer uid, Integer vid) {
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("vid", vid);
        UserVideo userVideo = userVideoMapper.selectOne(queryWrapper);

        if (userVideo == null || userVideo.getPlayTime() == null) {
            // 第一次播放，应该计数
            return true;
        }

        // 检查距离上次播放是否超过30分钟
        LocalDateTime lastPlayTime = userVideo.getPlayTime();
        LocalDateTime now = LocalDateTime.now();
        long minutesDiff = java.time.Duration.between(lastPlayTime, now).toMinutes();

        // 如果距离上次播放超过30分钟，则应该计数
        return minutesDiff >= 30;
    }

    @Override
    @Transactional
    public void updatePlayTime(Integer uid, Integer vid) {
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("vid", vid);
        UserVideo userVideo = userVideoMapper.selectOne(queryWrapper);

        if (userVideo == null) {
            // 创建新记录
            userVideo = createNewUserVideoRecord(uid, vid);
            userVideo.setPlay(1);
            userVideo.setPlayTime(LocalDateTime.now());
            userVideoMapper.insert(userVideo);
        } else {
            // 更新播放时间
            userVideo.setPlayTime(LocalDateTime.now());
            // 同时增加播放次数计数器（用于统计用户观看次数，不影响视频总播放量）
            if (userVideo.getPlay() == null) {
                userVideo.setPlay(1);
            } else {
                userVideo.setPlay(userVideo.getPlay() + 1);
            }
            userVideoMapper.updateById(userVideo);
        }
    }

    @Override
    public UserVideo getByUidAndVid(Integer uid, Integer vid) {
        QueryWrapper<UserVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid).eq("vid", vid);
        return userVideoMapper.selectOne(queryWrapper);
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