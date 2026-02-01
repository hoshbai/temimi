package com.temimi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.temimi.model.entity.Danmu;

import java.util.List;

/**
 * 弹幕服务接口
 */
public interface DanmuService extends IService<Danmu> {

    /**
     * 根据视频ID获取所有弹幕
     * @param vid 视频ID
     * @return 弹幕列表
     */
    List<Danmu> getDanmuByVid(Integer vid);

    /**
     * 根据视频ID和日期查询历史弹幕
     * @param vid 视频ID
     * @param date 日期 (格式: yyyy-MM-dd)
     * @return 弹幕列表
     */
    List<Danmu> getDanmuByVidAndDate(Integer vid, String date);

    /**
     * 发送弹幕
     * @param danmu 弹幕对象
     * @return 是否发送成功
     */
    boolean sendDanmu(Danmu danmu);

    /**
     * 发送弹幕 (供WebSocket处理器调用)
     * @param danmu 弹幕对象
     * @param uid 用户ID
     * @return 是否发送成功
     */
    boolean sendDanmu(Danmu danmu, Integer uid);

    /**
     * 删除弹幕
     * @param danmuId 弹幕ID
     * @return 是否删除成功
     */
    boolean deleteDanmu(Integer danmuId);
}