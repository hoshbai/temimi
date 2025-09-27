package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.constant.BusinessConstants;
import com.temimi.exception.BusinessException;
import com.temimi.exception.BusinessErrorCode;
import com.temimi.mapper.UserMapper;
import com.temimi.model.entity.User;
import com.temimi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(BusinessErrorCode.USER_LOGIN_FAILED);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(BusinessErrorCode.USER_LOGIN_FAILED);
        }

        if (user.getState() == BusinessConstants.USER_STATE_BANNED) {
            throw new BusinessException(BusinessErrorCode.USER_ACCOUNT_BANNED);
        } else if (user.getState() == BusinessConstants.USER_STATE_DELETED) {
            throw new BusinessException(BusinessErrorCode.USER_ACCOUNT_BANNED, "账号已注销");
        }

        user.setPassword(null);
        return user;
    }

    @Override
    public boolean register(User user) {
        User existingUser = userMapper.selectByUsername(user.getUsername());
        if (existingUser != null) {
            throw new BusinessException(BusinessErrorCode.USER_ALREADY_EXISTS);
        }

        User existingNickname = userMapper.selectByNickname(user.getNickname());
        if (existingNickname != null) {
            throw new BusinessException(BusinessErrorCode.USER_ALREADY_EXISTS, "昵称已存在");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        user.setExp(BusinessConstants.STATS_INITIAL_VALUE);
        user.setCoin(0.0);
        user.setVip(BusinessConstants.STATS_INITIAL_VALUE);
        user.setState(BusinessConstants.USER_STATE_NORMAL);
        user.setRole(BusinessConstants.STATS_INITIAL_VALUE);
        user.setAuth(BusinessConstants.STATS_INITIAL_VALUE);
        user.setCreateDate(LocalDateTime.now());

        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException(BusinessErrorCode.DATABASE_ERROR, "注册失败，请稍后重试");
        }

        return true;
    }

    @Override
    public boolean updateUserInfo(User user) {
        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new BusinessException(BusinessErrorCode.DATABASE_ERROR, "更新用户信息失败");
        }
        return true;
    }

    @Override
    public List<User> searchUsers(String keyword) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("username", keyword)
                .or()
                .like("nickname", keyword)
                .eq("state", BusinessConstants.USER_STATE_NORMAL);
        return userMapper.selectList(queryWrapper);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public User getUserById(Integer uid) {
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BusinessException(BusinessErrorCode.USER_NOT_FOUND);
        }

        user.setPassword(null);
        return user;
    }
}