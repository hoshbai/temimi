package com.temimi.controller.user;

import com.temimi.model.vo.ApiResult;
import com.temimi.service.CoinService;
import com.temimi.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 硬币相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/coin")
public class CoinController {

    @Autowired
    private CoinService coinService;

    /**
     * 每日登录奖励
     */
    @PostMapping("/daily-reward")
    public ApiResult<?> dailyReward() {
        Integer uid = SecurityUtil.getCurrentUserIdRequired();
        boolean rewarded = coinService.dailyLoginReward(uid);
        
        if (rewarded) {
            return ApiResult.success("获得每日登录奖励 1 硬币");
        } else {
            return ApiResult.success("今日已领取奖励");
        }
    }

    /**
     * 检查今日是否已登录
     */
    @GetMapping("/check-today-login")
    public ApiResult<Boolean> checkTodayLogin() {
        Integer uid = SecurityUtil.getCurrentUserIdRequired();
        boolean hasLogin = coinService.hasTodayLogin(uid);
        return ApiResult.success(hasLogin);
    }

    /**
     * 获取用户硬币余额
     */
    @GetMapping("/balance")
    public ApiResult<Double> getBalance() {
        Integer uid = SecurityUtil.getCurrentUserIdRequired();
        Double balance = coinService.getUserCoinBalance(uid);
        return ApiResult.success(balance);
    }
}
