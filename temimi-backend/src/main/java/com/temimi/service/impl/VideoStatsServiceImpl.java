package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.mapper.VideoStatsMapper;
import com.temimi.model.entity.VideoStats;
import com.temimi.service.VideoStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VideoStatsServiceImpl extends ServiceImpl<VideoStatsMapper, VideoStats> implements VideoStatsService {

    @Autowired
    private VideoStatsMapper videoStatsMapper;

    @Override
    @Transactional
    public boolean initStats(Integer vid) {
        VideoStats stats = new VideoStats();
        stats.setVid(vid);
        stats.setPlay(0); // 使用 Long 类型，避免播放量溢出
        stats.setDanmu(0);
        stats.setGood(0);
        stats.setBad(0);
        stats.setCoin(0);
        stats.setCollect(0);
        stats.setShare(0);
        stats.setComment(0);

        int result = videoStatsMapper.insert(stats);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean incrementPlayCount(Integer vid) {
        UpdateWrapper<VideoStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("vid", vid)
                .setSql("play = play + 1"); // 使用 setSql 避免并发问题

        int result = videoStatsMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean incrementLikeCount(Integer vid) {
        UpdateWrapper<VideoStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("vid", vid)
                .setSql("good = good + 1");

        int result = videoStatsMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean decrementLikeCount(Integer vid) {
        UpdateWrapper<VideoStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("vid", vid)
                .setSql("good = GREATEST(good - 1, 0)"); // 确保不小于0

        int result = videoStatsMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean decrementCollectCount(Integer vid) {
        UpdateWrapper<VideoStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("vid", vid)
                .setSql("collect = GREATEST(collect - 1, 0)"); // 确保不小于0

        int result = videoStatsMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean incrementDanmuCount(Integer vid) {
        UpdateWrapper<VideoStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("vid", vid)
                .setSql("danmu = danmu + 1");

        int result = videoStatsMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean incrementComment(Integer vid) {
        UpdateWrapper<VideoStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("vid", vid)
                .setSql("comment = comment + 1");

        int result = videoStatsMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean incrementCommentCount(Integer vid) {
        return incrementComment(vid);
    }

    @Override
    @Transactional
    public boolean decrementComment(Integer vid) {
        UpdateWrapper<VideoStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("vid", vid)
                .setSql("comment = GREATEST(comment - 1, 0)");

        int result = videoStatsMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean incrementCoinCount(Integer vid) {
        UpdateWrapper<VideoStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("vid", vid)
                .setSql("coin = coin + 1");

        int result = videoStatsMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean incrementCollectCount(Integer vid) {
        UpdateWrapper<VideoStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("vid", vid)
                .setSql("collect = collect + 1");

        int result = videoStatsMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean incrementShareCount(Integer vid) {
        UpdateWrapper<VideoStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("vid", vid)
                .setSql("share = share + 1");

        int result = videoStatsMapper.update(null, updateWrapper);
        return result > 0;
    }

    @Override
    public VideoStats getStatsByVid(Integer vid) {
        return videoStatsMapper.selectById(vid);
    }
}