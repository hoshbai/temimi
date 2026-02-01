package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.mapper.UserDailyLoginMapper;
import com.temimi.model.entity.User;
import com.temimi.model.entity.UserDailyLogin;
import com.temimi.service.UserDailyLoginService;
import com.temimi.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户每日登录服务实现类
 */
@Service
public class UserDailyLoginServiceImpl extends ServiceImpl<UserDailyLoginMapper, UserDailyLogin> implements UserDailyLoginService {

    private static final Logger logger = LoggerFactory.getLogger(UserDailyLoginServiceImpl.class);

    // 每日登录奖励的硬币数量
    private static final int DAILY_COIN_REWARD = 1;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public boolean processDailyLogin(Integer uid) {
        try {
            // 检查今天是否已经登录过
            if (hasLoginToday(uid)) {
                logger.debug("用户 {} 今天已经登录过，不重复奖励", uid);
                return false;
            }

            // 创建今日登录记录
            UserDailyLogin dailyLogin = new UserDailyLogin();
            dailyLogin.setUid(uid);
            dailyLogin.setLoginDate(LocalDate.now());
            dailyLogin.setCoinRewarded(1); // 标记已发放奖励
            dailyLogin.setCreateTime(LocalDateTime.now());

            // 保存登录记录
            boolean saved = this.save(dailyLogin);

            if (saved) {
                // 给用户增加硬币
                User user = userService.getById(uid);
                if (user != null) {
                    double currentCoin = user.getCoin() != null ? user.getCoin() : 0.0;
                    user.setCoin(currentCoin + DAILY_COIN_REWARD);
                    userService.updateById(user);

                    logger.info("用户 {} 每日登录奖励成功，获得 {} 个硬币，当前硬币: {}", 
                        uid, DAILY_COIN_REWARD, user.getCoin());
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            logger.error("处理用户每日登录奖励失败, uid: {}, error: {}", uid, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean hasLoginToday(Integer uid) {
        QueryWrapper<UserDailyLogin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                   .eq("login_date", LocalDate.now());

        return this.count(queryWrapper) > 0;
    }

    @Override
    public int getTodayCoinReward(Integer uid) {
        QueryWrapper<UserDailyLogin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid)
                   .eq("login_date", LocalDate.now());

        UserDailyLogin dailyLogin = this.getOne(queryWrapper);
        return dailyLogin != null ? dailyLogin.getCoinRewarded() : 0;
    }
}
