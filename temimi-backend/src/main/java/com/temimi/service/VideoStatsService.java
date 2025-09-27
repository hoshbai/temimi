package com.temimi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.temimi.model.entity.VideoStats;

/**
 * 视频数据统计服务接口
 */
public interface VideoStatsService extends IService<VideoStats> {

    /**
     * 初始化视频统计数据
     * 在视频投稿成功后调用
     * @param vid 视频ID
     * @return 是否初始化成功
     */
    boolean initStats(Integer vid);

    /**
     * 减少视频收藏数
     * @param vid 视频ID
     * @return 是否更新成功
     */
    boolean decrementCollectCount(Integer vid);
    /**
     * 增加视频播放量
     * @param vid 视频ID
     * @return 是否更新成功
     */
    boolean incrementPlayCount(Integer vid);

    /**
     * 增加视频点赞数
     * @param vid 视频ID
     * @return 是否更新成功
     */
    boolean incrementLikeCount(Integer vid);

    /**
     * 减少视频点赞数 (用户取消点赞时调用)
     * @param vid 视频ID
     * @return 是否更新成功
     */
    boolean decrementLikeCount(Integer vid);

    /**
     * 增加视频弹幕数
     * @param vid 视频ID
     * @return 是否更新成功
     */
    boolean incrementDanmuCount(Integer vid);

    /**
     * 增加视频评论数
     * @param vid 视频ID
     * @return 是否更新成功
     */
    boolean incrementCommentCount(Integer vid);

    /**
     * 增加视频投币数
     * @param vid 视频ID
     * @return 是否更新成功
     */
    boolean incrementCoinCount(Integer vid);

    /**
     * 增加视频收藏数
     * @param vid 视频ID
     * @return 是否更新成功
     */
    boolean incrementCollectCount(Integer vid);

    /**
     * 增加视频分享数
     * @param vid 视频ID
     * @return 是否更新成功
     */
    boolean incrementShareCount(Integer vid);

    /**
     * 增加评论数
     */
    boolean incrementComment(Integer vid);

    /**
     * 减少评论数
     */
    boolean decrementComment(Integer vid);

    /**
     * 根据视频ID获取统计数据
     * @param vid 视频ID
     * @return VideoStats 对象
     */
    VideoStats getStatsByVid(Integer vid);
}