package com.temimi.service;

import com.temimi.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户服务接口
 * 继承 IService 可以直接获得 MyBatis-Plus 提供的基础CRUD方法
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码（前端传来的原始密码）
     * @return 登录成功的用户信息 (通常不包含密码)
     * @throws RuntimeException 登录失败时抛出异常
     */
    User login(String username, String password);

    /**
     * 用户注册
     * @param user 用户对象 (包含用户名、密码、昵称等)
     * @return 注册是否成功
     */
    boolean register(User user);

    /**
     * 根据用户ID更新用户信息
     * @param user 用户对象 (包含要更新的字段)
     * @return 更新是否成功
     */
    boolean updateUserInfo(User user);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    User getUserByUsername(String username);

    /**
     * 根据用户ID查询用户
     * @param uid 用户ID
     * @return 用户对象
     */
    User getUserById(Integer uid);
    /**
     * 根据用户名或昵称模糊搜索用户
     * @param keyword 搜索关键词
     * @return 用户列表
     */
    List<User> searchUsers(String keyword);
}