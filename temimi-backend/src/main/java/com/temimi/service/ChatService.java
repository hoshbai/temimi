package com.temimi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.temimi.model.entity.Chat;
import java.util.List;

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
}