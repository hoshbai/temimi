package com.temimi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.temimi.model.entity.Message;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService extends IService<Message> {

    /**
     * 根据消息类型获取消息列表（分页）
     * @param uid 接收者用户ID
     * @param type 消息类型 (reply, like, system, follow)
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 消息分页对象
     */
    Page<Message> getMessagesByType(Integer uid, String type, Integer pageNum, Integer pageSize);

    /**
     * 标记消息为已读
     * @param uid 用户ID
     * @param type 消息类型
     * @param messageIds 消息ID列表（如果为空则标记该类型所有消息为已读）
     * @return 是否标记成功
     */
    boolean markAsRead(Integer uid, String type, List<Integer> messageIds);

    /**
     * 创建新消息
     * @param message 消息对象
     * @return 是否创建成功
     */
    boolean createMessage(Message message);

    /**
     * 删除消息
     * @param uid 用户ID
     * @param messageId 消息ID
     * @return 是否删除成功
     */
    boolean deleteMessage(Integer uid, Integer messageId);

    /**
     * 获取指定类型的未读消息数
     * @param uid 用户ID
     * @param type 消息类型
     * @return 未读消息数
     */
    Integer getUnreadCountByType(Integer uid, String type);

    /**
     * 批量获取各类型未读消息数
     * @param uid 用户ID
     * @return 各类型未读消息数的Map
     */
    java.util.Map<String, Integer> getAllUnreadCounts(Integer uid);

    /**
     * 获取私信未读数（从chat表统计）
     * @param uid 用户ID
     * @return 私信未读总数
     */
    Integer getWhisperUnreadCount(Integer uid);
}
