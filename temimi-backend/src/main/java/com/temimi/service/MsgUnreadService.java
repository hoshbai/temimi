package com.temimi.service;

import com.temimi.model.entity.MsgUnread;

/**
 * 消息未读数服务接口
 */
public interface MsgUnreadService {

    /**
     * 获取用户未读消息数
     * @param uid 用户ID
     * @return 未读消息统计
     */
    MsgUnread getUnreadCountByUid(Integer uid);

    /**
     * 清除某一类型的未读数
     * @param uid 用户ID
     * @param category 类别 reply/at/love/system/whisper/dynamic
     * @return 是否成功
     */
    boolean clearUnreadCount(Integer uid, String category);

    /**
     * 增加某一类型的未读数
     * @param uid 用户ID
     * @param category 类别
     * @return 是否成功
     */
    boolean incrementUnreadCount(Integer uid, String category);

    /**
     * 减少某一类型的未读数
     * @param uid 用户ID
     * @param category 类别
     * @param count 减少的数量
     * @return 是否成功
     */
    boolean decrementUnreadCount(Integer uid, String category, int count);

    /**
     * 初始化用户未读数记录
     * @param uid 用户ID
     */
    void initUnreadRecord(Integer uid);
}
