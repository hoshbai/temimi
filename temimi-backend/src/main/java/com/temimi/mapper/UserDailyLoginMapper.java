package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.UserDailyLogin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户每日登录Mapper接口
 */
@Mapper
public interface UserDailyLoginMapper extends BaseMapper<UserDailyLogin> {
}
