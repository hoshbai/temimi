package com.temimi.service;

/**
 * 硬币服务接口
 */
public interface CoinService {

    /**
     * 每日登录奖励
     * @param uid 用户ID
     * @return 是否获得奖励（今天首次登录返回true）
     */
    boolean dailyLoginReward(Integer uid);

    /**
     * 用户投币给视频
     * @param uid 用户ID
     * @param vid 视频ID
     * @param coinNum 投币数量（1或2）
     * @return 是否成功
     */
    boolean coinToVideo(Integer uid, Integer vid, Integer coinNum);

    /**
     * 检查视频收藏里程碑并发放奖励
     * @param vid 视频ID
     */
    void checkCollectMilestone(Integer vid);

    /**
     * 获取用户今日是否已登录
     * @param uid 用户ID
     * @return 是否已登录
     */
    boolean hasTodayLogin(Integer uid);

    /**
     * 获取用户硬币余额
     * @param uid 用户ID
     * @return 硬币余额
     */
    Double getUserCoinBalance(Integer uid);
}
