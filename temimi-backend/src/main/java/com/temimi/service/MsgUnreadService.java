package com.temimi.service;

import com.temimi.model.entity.MsgUnread;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 消息未读数服务接口
 */
public interface MsgUnreadService extends IService<MsgUnread> {

    /**
     * 根据用户ID获取未读消息数
     * @param uid 用户ID
     * @return MsgUnread 对象
     */
    MsgUnread getUnreadCountByUid(Integer uid);

    /**
     * 清除指定类型的未读数
     * @param uid 用户ID
     * @param category 消息类型 (reply, at, love, system, whisper, dynamic)
     * @return 是否清除成功
     */
    boolean clearUnreadCount(Integer uid, String category);

    /**
     * 增加指定类型的未读数
     * @param uid 用户ID
     * @param category 消息类型
     * @return 是否增加成功
     */
    boolean incrementUnreadCount(Integer uid, String category);
}