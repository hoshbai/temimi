package com.temimi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.temimi.model.entity.UserDailyLogin;

/**
 * 用户每日登录服务接口
 */
public interface UserDailyLoginService extends IService<UserDailyLogin> {

    /**
     * 处理用户每日登录奖励
     * 如果用户今天第一次登录，则奖励硬币
     * 
     * @param uid 用户ID
     * @return 是否获得了今日奖励
     */
    boolean processDailyLogin(Integer uid);

    /**
     * 检查用户今天是否已经登录过
     * 
     * @param uid 用户ID
     * @return 今天是否已登录
     */
    boolean hasLoginToday(Integer uid);

    /**
     * 获取用户今天获得的硬币奖励数量
     * 
     * @param uid 用户ID
     * @return 硬币奖励数量，如果今天未登录则返回0
     */
    int getTodayCoinReward(Integer uid);
}
