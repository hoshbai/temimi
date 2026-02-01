package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.mapper.UserFollowMapper;
import com.temimi.mapper.UserMapper;
import com.temimi.model.dto.UserDto;
import com.temimi.model.entity.User;
import com.temimi.model.entity.UserFollow;
import com.temimi.service.UserFollowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户关注Service实现类
 */
@Slf4j
@Service
public class UserFollowServiceImpl implements UserFollowService {

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean followUser(Integer followerId, Integer followingId, boolean isFollow) {
        // 不能关注自己
        if (followerId.equals(followingId)) {
            throw new RuntimeException("不能关注自己");
        }

        // 查询是否已存在关注记录
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId)
                   .eq(UserFollow::getFollowingId, followingId);
        UserFollow existingFollow = userFollowMapper.selectOne(queryWrapper);

        if (isFollow) {
            // 关注操作
            if (existingFollow == null) {
                // 新增关注记录
                UserFollow userFollow = new UserFollow();
                userFollow.setFollowerId(followerId);
                userFollow.setFollowingId(followingId);
                userFollow.setFollowTime(LocalDateTime.now());
                userFollow.setStatus(1);

                // 检查是否互相关注
                boolean isMutual = isFollowing(followingId, followerId);
                userFollow.setIsMutual(isMutual ? 1 : 0);

                int result = userFollowMapper.insert(userFollow);

                // 如果是互相关注，更新对方的记录
                if (isMutual) {
                    updateMutualStatus(followingId, followerId, true);
                }

                return result > 0;
            } else if (existingFollow.getStatus() == 0) {
                // 重新关注
                existingFollow.setStatus(1);
                existingFollow.setFollowTime(LocalDateTime.now());

                // 检查是否互相关注
                boolean isMutual = isFollowing(followingId, followerId);
                existingFollow.setIsMutual(isMutual ? 1 : 0);

                int result = userFollowMapper.updateById(existingFollow);

                // 如果是互相关注，更新对方的记录
                if (isMutual) {
                    updateMutualStatus(followingId, followerId, true);
                }

                return result > 0;
            }
            return true; // 已经关注
        } else {
            // 取消关注操作
            if (existingFollow != null && existingFollow.getStatus() == 1) {
                existingFollow.setStatus(0);
                existingFollow.setIsMutual(0);
                int result = userFollowMapper.updateById(existingFollow);

                // 取消互相关注状态
                updateMutualStatus(followingId, followerId, false);

                return result > 0;
            }
            return true; // 已经是未关注状态
        }
    }

    /**
     * 更新互相关注状态
     */
    private void updateMutualStatus(Integer followerId, Integer followingId, boolean isMutual) {
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId)
                   .eq(UserFollow::getFollowingId, followingId)
                   .eq(UserFollow::getStatus, 1);
        UserFollow userFollow = userFollowMapper.selectOne(queryWrapper);

        if (userFollow != null) {
            userFollow.setIsMutual(isMutual ? 1 : 0);
            userFollowMapper.updateById(userFollow);
        }
    }

    @Override
    public boolean isFollowing(Integer followerId, Integer followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }

        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId)
                   .eq(UserFollow::getFollowingId, followingId)
                   .eq(UserFollow::getStatus, 1);
        return userFollowMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public List<UserDto> getFansList(Integer uid, Integer page, Integer pageSize) {
        // 分页查询粉丝ID列表
        Page<UserFollow> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowingId, uid)
                   .eq(UserFollow::getStatus, 1)
                   .orderByDesc(UserFollow::getFollowTime);

        Page<UserFollow> followPage = userFollowMapper.selectPage(pageParam, queryWrapper);

        if (followPage.getRecords().isEmpty()) {
            return new ArrayList<>();
        }

        // 获取粉丝用户信息
        List<Integer> fanIds = followPage.getRecords().stream()
                .map(UserFollow::getFollowerId)
                .collect(Collectors.toList());

        List<User> fans = userMapper.selectBatchIds(fanIds);

        // 转换为UserDto并填充关注状态
        return fans.stream().map(user -> {
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(user, userDto);
            userDto.setIsFollowed(isFollowing(uid, user.getUid())); // 当前用户是否关注了这个粉丝
            userDto.setFansCount(getFansCount(user.getUid()));
            userDto.setFollowingCount(getFollowingCount(user.getUid()));
            return userDto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getFollowingList(Integer uid, Integer page, Integer pageSize) {
        // 分页查询关注ID列表
        Page<UserFollow> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, uid)
                   .eq(UserFollow::getStatus, 1)
                   .orderByDesc(UserFollow::getFollowTime);

        Page<UserFollow> followPage = userFollowMapper.selectPage(pageParam, queryWrapper);

        if (followPage.getRecords().isEmpty()) {
            return new ArrayList<>();
        }

        // 获取被关注用户信息
        List<Integer> followingIds = followPage.getRecords().stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toList());

        List<User> followingUsers = userMapper.selectBatchIds(followingIds);

        // 转换为UserDto并填充关注状态
        return followingUsers.stream().map(user -> {
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(user, userDto);
            userDto.setIsFollowed(true); // 当前列表中的用户都是已关注的
            userDto.setFansCount(getFansCount(user.getUid()));
            userDto.setFollowingCount(getFollowingCount(user.getUid()));
            return userDto;
        }).collect(Collectors.toList());
    }

    @Override
    public Integer getFansCount(Integer uid) {
        return userFollowMapper.countFans(uid);
    }

    @Override
    public Integer getFollowingCount(Integer uid) {
        return userFollowMapper.countFollowing(uid);
    }

    @Override
    public List<Integer> getFollowingIds(Integer uid) {
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, uid)
                   .eq(UserFollow::getStatus, 1)
                   .select(UserFollow::getFollowingId);
        
        List<UserFollow> follows = userFollowMapper.selectList(queryWrapper);
        return follows.stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toList());
    }
}
