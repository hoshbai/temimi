package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.mapper.DanmuMapper;
import com.temimi.model.entity.Danmu;
import com.temimi.service.DanmuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DanmuServiceImpl extends ServiceImpl<DanmuMapper, Danmu> implements DanmuService {

    @Autowired
    private DanmuMapper danmuMapper;

    @Override
    public List<Danmu> getDanmuByVidAndDate(Integer vid, String date) {
        // 将日期字符串转换为 LocalDateTime 范围
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime startOfDay = LocalDateTime.parse(date + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime endOfDay = LocalDateTime.parse(date + "T23:59:59", DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        QueryWrapper<Danmu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vid", vid)
                .eq("state", 1) // 只查询已过审的弹幕
                .between("create_date", startOfDay, endOfDay) // 在指定日期范围内
                .orderByAsc("time_point"); // 按视频时间点排序

        return danmuMapper.selectList(queryWrapper);
    }

    @Override
    public boolean sendDanmu(Danmu danmu, Integer uid) {
        // 1. 设置用户ID
        danmu.setUid(uid);

        // 2. 设置发送时间
        danmu.setCreateDate(LocalDateTime.now());

        // 3. 设置默认状态 (1: 默认过审)
        danmu.setState(1);

        // 4. 插入数据库
        int result = danmuMapper.insert(danmu);

        // 5. 更新 video_stats 表的弹幕数 (此处省略)

        return result > 0;
    }
}