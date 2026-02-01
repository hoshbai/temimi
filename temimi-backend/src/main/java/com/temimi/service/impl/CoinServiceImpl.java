package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.temimi.mapper.*;
import com.temimi.model.entity.*;
import com.temimi.service.CoinService;
import com.temimi.service.SystemNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 硬币服务实现类
 */
@Slf4j
@Service
public class CoinServiceImpl implements CoinService {

    @Autowired
    private UserDailyLoginMapper userDailyLoginMapper;

    @Autowired
    private CoinTransactionMapper coinTransactionMapper;

    @Autowired
    private VideoCollectMilestoneMapper videoCollectMilestoneMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoStatsMapper videoStatsMapper;

    @Autowired
    private UserVideoMapper userVideoMapper;

    @Autowired
    @Lazy
    private SystemNotificationService systemNotificationService;

    /**
     * 每日登录奖励
     */
    @Override
    @Transactional
    public boolean dailyLoginReward(Integer uid) {
        LocalDate today = LocalDate.now();
        
        // 检查今天是否已经登录过
        LambdaQueryWrapper<UserDailyLogin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDailyLogin::getUid, uid)
               .eq(UserDailyLogin::getLoginDate, today);
        
        UserDailyLogin existingLogin = userDailyLoginMapper.selectOne(wrapper);
        
        if (existingLogin != null) {
            // 今天已经登录过
            if (existingLogin.getCoinRewarded() == 1) {
                return false; // 已经领取过奖励
            }
        }
        
        // 记录登录
        if (existingLogin == null) {
            UserDailyLogin dailyLogin = new UserDailyLogin();
            dailyLogin.setUid(uid);
            dailyLogin.setLoginDate(today);
            dailyLogin.setCoinRewarded(1);
            dailyLogin.setCreateTime(LocalDateTime.now());
            userDailyLoginMapper.insert(dailyLogin);
        } else {
            existingLogin.setCoinRewarded(1);
            userDailyLoginMapper.updateById(existingLogin);
        }
        
        // 给用户增加1个硬币
        User user = userMapper.selectById(uid);
        if (user != null) {
            user.setCoin(user.getCoin() + 1);
            userMapper.updateById(user);
            
            // 记录交易
            CoinTransaction transaction = new CoinTransaction();
            transaction.setUid(uid);
            transaction.setAmount(1.0);
            transaction.setType(1); // 1=每日登录
            transaction.setDescription("每日登录奖励");
            transaction.setCreateTime(LocalDateTime.now());
            coinTransactionMapper.insert(transaction);
            
            log.info("用户 {} 获得每日登录奖励 1 硬币", uid);
            
            // 发送系统通知
            systemNotificationService.sendDailyLoginRewardNotification(uid, 1);
            
            return true;
        }
        
        return false;
    }

    /**
     * 用户投币给视频
     */
    @Override
    @Transactional
    public boolean coinToVideo(Integer uid, Integer vid, Integer coinNum) {
        if (coinNum < 1 || coinNum > 2) {
            throw new RuntimeException("投币数量只能是1或2");
        }
        
        // 检查用户硬币是否足够
        User user = userMapper.selectById(uid);
        if (user == null || user.getCoin() < coinNum) {
            throw new RuntimeException("硬币不足");
        }
        
        // 获取视频信息
        Video video = videoMapper.selectById(vid);
        if (video == null) {
            throw new RuntimeException("视频不存在");
        }
        
        // 扣除用户硬币
        user.setCoin(user.getCoin() - coinNum);
        userMapper.updateById(user);
        
        // 记录用户支出交易
        CoinTransaction userTransaction = new CoinTransaction();
        userTransaction.setUid(uid);
        userTransaction.setAmount(-coinNum.doubleValue());
        userTransaction.setType(2); // 2=投币给视频
        userTransaction.setRelatedVid(vid);
        userTransaction.setRelatedUid(video.getUid());
        userTransaction.setDescription("投币给视频: " + video.getTitle());
        userTransaction.setCreateTime(LocalDateTime.now());
        coinTransactionMapper.insert(userTransaction);
        
        // UP主获得10%的硬币奖励
        double upReward = coinNum * 0.1;
        User upUser = userMapper.selectById(video.getUid());
        if (upUser != null) {
            upUser.setCoin(upUser.getCoin() + upReward);
            userMapper.updateById(upUser);
            
            // 记录UP主收入交易
            CoinTransaction upTransaction = new CoinTransaction();
            upTransaction.setUid(video.getUid());
            upTransaction.setAmount(upReward);
            upTransaction.setType(3); // 3=收到投币奖励
            upTransaction.setRelatedVid(vid);
            upTransaction.setRelatedUid(uid);
            upTransaction.setDescription("收到投币奖励 (10%)");
            upTransaction.setCreateTime(LocalDateTime.now());
            coinTransactionMapper.insert(upTransaction);
            
            log.info("UP主 {} 从视频 {} 获得投币奖励 {} 硬币", video.getUid(), vid, upReward);
            
            // 发送系统通知给UP主
            systemNotificationService.sendCoinRewardNotification(video.getUid(), uid, vid, video.getTitle(), upReward);
        }
        
        log.info("用户 {} 向视频 {} 投币 {} 个", uid, vid, coinNum);
        return true;
    }

    /**
     * 检查视频收藏里程碑并发放奖励
     */
    @Override
    @Transactional
    public void checkCollectMilestone(Integer vid) {
        // 获取视频统计信息
        VideoStats stats = videoStatsMapper.selectById(vid);
        if (stats == null) {
            return;
        }
        
        int collectCount = stats.getCollect();
        
        // 检查是否达到50的倍数
        if (collectCount < 50 || collectCount % 50 != 0) {
            return;
        }
        
        // 检查这个里程碑是否已经发放过奖励
        LambdaQueryWrapper<VideoCollectMilestone> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoCollectMilestone::getVid, vid)
               .eq(VideoCollectMilestone::getMilestone, collectCount);
        
        VideoCollectMilestone existingMilestone = videoCollectMilestoneMapper.selectOne(wrapper);
        
        if (existingMilestone != null && existingMilestone.getRewarded() == 1) {
            return; // 已经发放过奖励
        }
        
        // 获取视频信息
        Video video = videoMapper.selectById(vid);
        if (video == null) {
            return;
        }
        
        // 给UP主发放1个硬币奖励
        User upUser = userMapper.selectById(video.getUid());
        if (upUser != null) {
            upUser.setCoin(upUser.getCoin() + 1);
            userMapper.updateById(upUser);
            
            // 记录交易
            CoinTransaction transaction = new CoinTransaction();
            transaction.setUid(video.getUid());
            transaction.setAmount(1.0);
            transaction.setType(4); // 4=收藏奖励
            transaction.setRelatedVid(vid);
            transaction.setDescription("视频达到 " + collectCount + " 收藏里程碑奖励");
            transaction.setCreateTime(LocalDateTime.now());
            coinTransactionMapper.insert(transaction);
            
            log.info("视频 {} 达到 {} 收藏，UP主 {} 获得 1 硬币奖励", vid, collectCount, video.getUid());
            
            // 发送系统通知
            systemNotificationService.sendCollectMilestoneNotification(video.getUid(), vid, video.getTitle(), collectCount, 1);
        }
        
        // 记录里程碑
        if (existingMilestone == null) {
            VideoCollectMilestone milestone = new VideoCollectMilestone();
            milestone.setVid(vid);
            milestone.setMilestone(collectCount);
            milestone.setRewarded(1);
            milestone.setRewardTime(LocalDateTime.now());
            videoCollectMilestoneMapper.insert(milestone);
        } else {
            existingMilestone.setRewarded(1);
            existingMilestone.setRewardTime(LocalDateTime.now());
            videoCollectMilestoneMapper.updateById(existingMilestone);
        }
    }

    /**
     * 获取用户今日是否已登录
     */
    @Override
    public boolean hasTodayLogin(Integer uid) {
        LocalDate today = LocalDate.now();
        
        LambdaQueryWrapper<UserDailyLogin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDailyLogin::getUid, uid)
               .eq(UserDailyLogin::getLoginDate, today);
        
        UserDailyLogin dailyLogin = userDailyLoginMapper.selectOne(wrapper);
        return dailyLogin != null && dailyLogin.getCoinRewarded() == 1;
    }

    /**
     * 获取用户硬币余额
     */
    @Override
    public Double getUserCoinBalance(Integer uid) {
        User user = userMapper.selectById(uid);
        if (user != null && user.getCoin() != null) {
            return user.getCoin();
        }
        return 0.0;
    }
}
