package com.temimi.service;

import com.temimi.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
     * 根据用户ID查询用户（安全版本，不抛异常）
     * @param uid 用户ID
     * @return 用户对象，不存在时返回null
     */
    User getUserByIdSafe(Integer uid);

    /**
     * 根据用户名或昵称模糊搜索用户
     * @param keyword 搜索关键词
     * @return 用户列表
     */
    List<User> searchUsers(String keyword);
    
    /**
     * 根据用户名检查用户是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 根据昵称检查用户是否存在
     * @param nickname 昵称
     * @return 是否存在
     */
    boolean existsByNickname(String nickname);
    
    /**
     * 上传用户头像
     * @param file 头像文件
     * @param uid 用户ID
     * @return 头像URL
     * @throws IOException 文件操作异常
     */
    String uploadAvatar(MultipartFile file, Integer uid) throws IOException;

    /**
     * 更新用户头像
     * @param uid 用户ID
     * @param avatarUrl 头像URL
     * @return 是否成功
     */
    boolean updateUserAvatar(Integer uid, String avatarUrl);
    
    /**
     * 验证用户密码
     * @param uid 用户ID
     * @param oldPassword 旧密码
     * @return 是否正确
     */
    boolean validatePassword(Integer uid, String oldPassword);
    
    /**
     * 修改用户密码
     * @param uid 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean updatePassword(Integer uid, String newPassword);

    /**
     * 上传用户空间背景图
     * @param file 背景图文件
     * @param uid 用户ID
     * @return 背景图URL
     * @throws IOException 文件操作异常
     */
    String uploadBackground(MultipartFile file, Integer uid) throws IOException;

    /**
     * 更新用户空间背景图
     * @param uid 用户ID
     * @param bgUrl 背景图URL
     * @return 是否成功
     */
    boolean updateBackground(Integer uid, String bgUrl);
}