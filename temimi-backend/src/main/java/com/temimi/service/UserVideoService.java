package com.temimi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.temimi.model.entity.UserVideo;

/**
 * 用户视频行为服务接口
 */
public interface UserVideoService extends IService<UserVideo> {

    /**
     * 记录或更新用户观看视频的行为
     * @param uid 用户ID
     * @param vid 视频ID
     * @return 是否成功
     */
    boolean recordPlay(Integer uid, Integer vid);

    /**
     * 用户点赞视频
     * @param uid 用户ID
     * @param vid 视频ID
     * @return 是否成功
     */
    boolean likeVideo(Integer uid, Integer vid);

    /**
     * 用户取消点赞视频
     * @param uid 用户ID
     * @param vid 视频ID
     * @return 是否成功
     */
    boolean unlikeVideo(Integer uid, Integer vid);

    /**
     * 用户投币
     * @param uid 用户ID
     * @param vid 视频ID
     * @param coinCount 投币数量 (1 or 2)
     * @return 是否成功
     */
    boolean coinVideo(Integer uid, Integer vid, Integer coinCount);

    /**
     * 用户收藏视频
     * @param uid 用户ID
     * @param vid 视频ID
     * @return 是否成功
     */
    boolean collectVideo(Integer uid, Integer vid);

    /**
     * 用户取消收藏视频
     * @param uid 用户ID
     * @param vid 视频ID
     * @return 是否成功
     */
    boolean uncollectVideo(Integer uid, Integer vid);

    /**
     * 判断是否应该增加播放量（防止短时间内重复计数）
     * @param uid 用户ID
     * @param vid 视频ID
     * @return 如果距离上次播放超过30分钟，返回true；否则返回false
     */
    boolean shouldIncrementPlayCount(Integer uid, Integer vid);

    /**
     * 更新用户的播放时间戳
     * @param uid 用户ID
     * @param vid 视频ID
     */
    void updatePlayTime(Integer uid, Integer vid);

    /**
     * 根据用户ID和视频ID获取用户视频关联记录
     * @param uid 用户ID
     * @param vid 视频ID
     * @return UserVideo对象，不存在则返回null
     */
    UserVideo getByUidAndVid(Integer uid, Integer vid);
}