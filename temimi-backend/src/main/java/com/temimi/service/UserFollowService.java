package com.temimi.service;

import com.temimi.model.dto.UserDto;

import java.util.List;

/**
 * 用户关注Service接口
 */
public interface UserFollowService {

    /**
     * 关注/取消关注用户
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @param isFollow true-关注  false-取消关注
     * @return 是否成功
     */
    boolean followUser(Integer followerId, Integer followingId, boolean isFollow);

    /**
     * 检查是否已关注某用户
     * @param followerId 关注者ID
     * @param followingId 被关注者ID
     * @return true-已关注 false-未关注
     */
    boolean isFollowing(Integer followerId, Integer followingId);

    /**
     * 获取用户的粉丝列表
     * @param uid 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 粉丝列表
     */
    List<UserDto> getFansList(Integer uid, Integer page, Integer pageSize);

    /**
     * 获取用户的关注列表
     * @param uid 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 关注列表
     */
    List<UserDto> getFollowingList(Integer uid, Integer page, Integer pageSize);

    /**
     * 获取用户的粉丝数量
     * @param uid 用户ID
     * @return 粉丝数量
     */
    Integer getFansCount(Integer uid);

    /**
     * 获取用户的关注数量
     * @param uid 用户ID
     * @return 关注数量
     */
    Integer getFollowingCount(Integer uid);

    /**
     * 获取用户关注的所有用户ID列表
     * @param uid 用户ID
     * @return 关注的用户ID列表
     */
    List<Integer> getFollowingIds(Integer uid);
}
