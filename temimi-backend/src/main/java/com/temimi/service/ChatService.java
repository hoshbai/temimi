package com.temimi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.temimi.model.entity.Chat;
import java.util.List;
import java.util.Map;

/**
 * 聊天服务接口
 */
public interface ChatService extends IService<Chat> {

    /**
     * 获取用户的聊天列表
     * @param userId 用户ID
     * @return 聊天列表
     */
    List<Chat> getChatListByUserId(Integer userId);

    /**
     * 获取带用户信息和最后一条消息的聊天列表
     * @param userId 用户ID
     * @param offset 偏移量
     * @return 包含聊天列表和是否还有更多的Map
     */
    Map<String, Object> getChatListWithUserInfo(Integer userId, Long offset);

    /**
     * 创建或获取聊天会话
     * @param userId 用户ID
     * @param anotherId 对方用户ID
     * @return 聊天会话
     */
    Chat createOrGetChat(Integer userId, Integer anotherId);

    /**
     * 更新聊天最新时间
     * @param chatId 聊天ID
     * @return 是否成功
     */
    boolean updateLatestTime(Integer chatId);

    /**
     * 增加未读消息数
     * @param chatId 聊天ID
     * @return 是否成功
     */
    boolean incrementUnreadCount(Integer chatId);

    /**
     * 清除未读消息数
     * @param chatId 聊天ID
     * @return 是否成功
     */
    boolean clearUnreadCount(Integer chatId);

    /**
     * 删除聊天会话
     * @param chatId 聊天ID
     * @param userId 操作用户ID
     * @return 是否成功
     */
    boolean deleteChat(Integer chatId, Integer userId);

    /**
     * 根据另一方用户ID删除聊天会话
     * @param userId 当前用户ID
     * @param anotherId 对方用户ID
     * @return 是否成功
     */
    boolean deleteChatByAnotherId(Integer userId, Integer anotherId);

    /**
     * 更新在线状态并清除未读消息
     * @param userId 当前用户ID
     * @param fromUid 对方用户ID
     */
    void updateOnlineStatus(Integer userId, Integer fromUid);

    /**
     * 更新离线状态
     * @param fromUid 离开的用户ID
     * @param toUid 对方用户ID
     */
    void updateOutlineStatus(Integer fromUid, Integer toUid);
}