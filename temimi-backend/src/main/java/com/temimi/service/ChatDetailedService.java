package com.temimi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.temimi.model.entity.ChatDetailed;

/**
 * 聊天详情服务接口
 */
public interface ChatDetailedService extends IService<ChatDetailed> {
    
    /**
     * 分页获取聊天记录
     * @param userId 用户ID
     * @param anotherId 对方用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页聊天记录
     */
    Page<ChatDetailed> getChatHistory(Integer userId, Integer anotherId, int pageNum, int pageSize);
    
    /**
     * 发送消息
     * @param userId 发送者ID
     * @param anotherId 接收者ID
     * @param content 消息内容
     * @return 是否成功
     */
    boolean sendMessage(Integer userId, Integer anotherId, String content);
    
    /**
     * 撤回消息
     * @param messageId 消息ID
     * @param userId 操作用户ID
     * @return 是否成功
     */
    boolean withdrawMessage(Integer messageId, Integer userId);
    
    /**
     * 删除消息（发送者删除）
     * @param messageId 消息ID
     * @param userId 操作用户ID
     * @return 是否成功
     */
    boolean deleteMessageBySender(Integer messageId, Integer userId);
    
    /**
     * 删除消息（接收者删除）
     * @param messageId 消息ID
     * @param userId 操作用户ID
     * @return 是否成功
     */
    boolean deleteMessageByReceiver(Integer messageId, Integer userId);
}