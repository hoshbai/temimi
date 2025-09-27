package com.temimi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.temimi.model.entity.VideoStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 视频数据统计表 Mapper 接口
 */
@Mapper
public interface VideoStatsMapper extends BaseMapper<VideoStats> {

    /**
     * 增加播放量
     * @param vid 视频ID
     */
    @Update("UPDATE video_stats SET play = play + 1 WHERE vid = #{vid}")
    void incrementPlay(@Param("vid") Integer vid);

    /**
     * 增加点赞数
     * @param vid 视频ID
     */
    @Update("UPDATE video_stats SET good = good + 1 WHERE vid = #{vid}")
    void incrementGood(@Param("vid") Integer vid);

    /**
     * 减少点赞数 (用于取消点赞)
     * @param vid 视频ID
     */
    @Update("UPDATE video_stats SET good = good - 1 WHERE vid = #{vid} AND good > 0")
    void decrementGood(@Param("vid") Integer vid);

    /**
     * 增加投币数
     * @param vid 视频ID
     */
    @Update("UPDATE video_stats SET coin = coin + 1 WHERE vid = #{vid}")
    void incrementCoin(@Param("vid") Integer vid);

    /**
     * 增加收藏数
     * @param vid 视频ID
     */
    @Update("UPDATE video_stats SET collect = collect + 1 WHERE vid = #{vid}")
    void incrementCollect(@Param("vid") Integer vid);

    /**
     * 减少收藏数 (用于取消收藏)
     * @param vid 视频ID
     */
    @Update("UPDATE video_stats SET collect = collect - 1 WHERE vid = #{vid} AND collect > 0")
    void decrementCollect(@Param("vid") Integer vid);

    /**
     * 增加评论数
     * @param vid 视频ID
     */
    @Update("UPDATE video_stats SET comment = comment + 1 WHERE vid = #{vid}")
    void incrementComment(@Param("vid") Integer vid);
}