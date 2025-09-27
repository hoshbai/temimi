package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户表 Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户实体
     */
    @Select("SELECT * FROM user WHERE username = #{username} AND state = 0")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据昵称查询用户
     * @param nickname 昵称
     * @return 用户实体
     */
    @Select("SELECT * FROM user WHERE nickname = #{nickname} AND state = 0")
    User selectByNickname(@Param("nickname") String nickname);
}