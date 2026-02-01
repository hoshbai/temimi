package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.UserVideo;
import org.apache.ibatis.annotations.*;

/**
 * 用户视频关联表 Mapper 接口
 */
@Mapper
public interface UserVideoMapper extends BaseMapper<UserVideo> {

    /**
     * 根据用户ID和视频ID查询关联记录
     * @param uid 用户ID
     * @param vid 视频ID
     * @return 关联记录
     */
    @Select("SELECT * FROM user_video WHERE uid = #{uid} AND vid = #{vid}")
    UserVideo selectByUidAndVid(@Param("uid") Integer uid, @Param("vid") Integer vid);

    /**
     * 点赞视频（幂等操作，使用 INSERT ... ON DUPLICATE KEY UPDATE）
     * 如果记录不存在则插入，如果已存在则更新点赞状态
     *
     * @param uid 用户ID
     * @param vid 视频ID
     * @return 影响的行数
     */
    @Insert("INSERT INTO user_video (uid, vid, love, love_time, play_time, play, coin, collect) " +
            "VALUES (#{uid}, #{vid}, 1, NOW(), NOW(), 0, 0, 0) " +
            "ON DUPLICATE KEY UPDATE love = 1, love_time = NOW()")
    int likeVideoIdempotent(@Param("uid") Integer uid, @Param("vid") Integer vid);

    /**
     * 取消点赞（幂等操作）
     *
     * @param uid 用户ID
     * @param vid 视频ID
     * @return 影响的行数
     */
    @Update("UPDATE user_video SET love = 0 WHERE uid = #{uid} AND vid = #{vid}")
    int unlikeVideo(@Param("uid") Integer uid, @Param("vid") Integer vid);

    /**
     * 收藏视频（幂等操作）
     *
     * @param uid 用户ID
     * @param vid 视频ID
     * @return 影响的行数
     */
    @Insert("INSERT INTO user_video (uid, vid, collect, play_time, play, love, coin) " +
            "VALUES (#{uid}, #{vid}, 1, NOW(), 0, 0, 0) " +
            "ON DUPLICATE KEY UPDATE collect = 1")
    int collectVideoIdempotent(@Param("uid") Integer uid, @Param("vid") Integer vid);

    /**
     * 取消收藏（幂等操作）
     *
     * @param uid 用户ID
     * @param vid 视频ID
     * @return 影响的行数
     */
    @Update("UPDATE user_video SET collect = 0 WHERE uid = #{uid} AND vid = #{vid}")
    int uncollectVideo(@Param("uid") Integer uid, @Param("vid") Integer vid);

    /**
     * 投币（幂等操作）- 已废弃，使用coinVideoIncrement
     *
     * @param uid 用户ID
     * @param vid 视频ID
     * @param coinCount 投币数量
     * @return 影响的行数
     */
    @Deprecated
    @Insert("INSERT INTO user_video (uid, vid, coin, coin_time, play_time, play, love, collect) " +
            "VALUES (#{uid}, #{vid}, #{coinCount}, NOW(), NOW(), 0, 0, 0) " +
            "ON DUPLICATE KEY UPDATE coin = #{coinCount}, coin_time = NOW()")
    int coinVideoIdempotent(@Param("uid") Integer uid, @Param("vid") Integer vid, @Param("coinCount") Integer coinCount);

    /**
     * 投币（累加操作）
     * 如果记录不存在则插入，如果已存在则累加投币数
     *
     * @param uid 用户ID
     * @param vid 视频ID
     * @param coinCount 投币数量
     * @return 影响的行数
     */
    @Insert("INSERT INTO user_video (uid, vid, coin, coin_time, play_time, play, love, collect) " +
            "VALUES (#{uid}, #{vid}, #{coinCount}, NOW(), NOW(), 0, 0, 0) " +
            "ON DUPLICATE KEY UPDATE coin = coin + #{coinCount}, coin_time = NOW()")
    int coinVideoIncrement(@Param("uid") Integer uid, @Param("vid") Integer vid, @Param("coinCount") Integer coinCount);
}