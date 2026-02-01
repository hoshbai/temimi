package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.constant.BusinessConstants;
import com.temimi.exception.BusinessException;
import com.temimi.exception.BusinessErrorCode;
import com.temimi.mapper.ChatMapper;
import com.temimi.mapper.MessageMapper;
import com.temimi.model.entity.Chat;
import com.temimi.model.entity.Message;
import com.temimi.service.MessageService;
import com.temimi.service.MsgUnreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private MsgUnreadService msgUnreadService;

    @Autowired
    private ChatMapper chatMapper;

    @Override
    public Page<Message> getMessagesByType(Integer uid, String type, Integer pageNum, Integer pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("to_uid", uid)
                .eq("type", type)
                .orderByDesc("create_time");

        return messageMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional
    public boolean markAsRead(Integer uid, String type, List<Integer> messageIds) {
        UpdateWrapper<Message> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("to_uid", uid)
                .eq("type", type)
                .eq("is_read", false);

        // 如果指定了消息ID列表，则只标记这些消息
        if (messageIds != null && !messageIds.isEmpty()) {
            updateWrapper.in("id", messageIds);
        }

        Message message = new Message();
        message.setIsRead(true);

        int result = messageMapper.update(message, updateWrapper);

        // 注意：未读数的更新由数据库触发器自动处理，这里不需要手动更新
        // 触发器会在 UPDATE message 时自动减少 msg_unread 表中的未读数

        // 即使没有消息需要更新（result == 0），也返回true，因为这不是错误
        // 这种情况说明所有消息都已经是已读状态了
        return true;
    }

    @Override
    @Transactional
    public boolean createMessage(Message message) {
        if (message.getCreateTime() == null) {
            message.setCreateTime(LocalDateTime.now());
        }
        if (message.getIsRead() == null) {
            message.setIsRead(false);
        }

        int result = messageMapper.insert(message);

        // 注意：未读数的增加由数据库触发器自动处理，这里不需要手动更新
        // 触发器会在 INSERT message 时自动增加 msg_unread 表中的未读数

        return result > 0;
    }

    @Override
    @Transactional
    public boolean deleteMessage(Integer uid, Integer messageId) {
        Message message = messageMapper.selectById(messageId);

        if (message == null) {
            throw new BusinessException(BusinessErrorCode.MESSAGE_NOT_FOUND, "消息不存在");
        }

        // 验证消息所属用户
        if (!message.getToUid().equals(uid)) {
            throw new BusinessException(BusinessErrorCode.MESSAGE_PERMISSION_DENIED, "无权删除该消息");
        }

        int result = messageMapper.deleteById(messageId);

        // 注意：未读数的减少由数据库触发器自动处理，这里不需要手动更新
        // 触发器会在 DELETE message 时自动减少 msg_unread 表中的未读数

        return result > 0;
    }

    @Override
    public Integer getUnreadCountByType(Integer uid, String type) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("to_uid", uid)
                .eq("type", type)
                .eq("is_read", false);
        return Math.toIntExact(messageMapper.selectCount(queryWrapper));
    }

    @Override
    public java.util.Map<String, Integer> getAllUnreadCounts(Integer uid) {
        // 从msg_unread表获取各类型未读数
        com.temimi.model.entity.MsgUnread msgUnread = msgUnreadService.getUnreadCountByUid(uid);

        java.util.Map<String, Integer> unreadCounts = new java.util.HashMap<>();
        if (msgUnread != null) {
            unreadCounts.put("reply", msgUnread.getReply());
            unreadCounts.put("at", msgUnread.getAt());
            unreadCounts.put("love", msgUnread.getLove());
            unreadCounts.put("system", msgUnread.getSystem());
            unreadCounts.put("whisper", msgUnread.getWhisper());
            unreadCounts.put("dynamic", msgUnread.getDynamic());
        } else {
            // 如果记录不存在，返回全0
            unreadCounts.put("reply", 0);
            unreadCounts.put("at", 0);
            unreadCounts.put("love", 0);
            unreadCounts.put("system", 0);
            unreadCounts.put("whisper", 0);
            unreadCounts.put("dynamic", 0);
        }

        return unreadCounts;
    }

    @Override
    public Integer getWhisperUnreadCount(Integer uid) {
        // 从chat表统计该用户所有会话的未读消息总数
        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", uid)
                .eq("is_deleted", 0);  // 未删除的会话

        List<Chat> chats = chatMapper.selectList(queryWrapper);

        // 累加所有会话的未读数
        int totalUnread = 0;
        if (chats != null && !chats.isEmpty()) {
            for (Chat chat : chats) {
                totalUnread += chat.getUnread();
            }
        }

        return totalUnread;
    }

    /**
     * 将消息类型映射到未读数类别
     * Message表的type字段：reply, at, like, system, follow
     * MsgUnread表的字段：reply, at, love, system, whisper, dynamic
     */
    private String mapMessageTypeToUnreadCategory(String messageType) {
        switch (messageType) {
            case "reply":
                return "reply";
            case "at":
                return "at";  // @消息
            case "like":
                return "love";  // 消息表的like对应未读表的love
            case "system":
                return "system";
            case "follow":
                return "dynamic";  // 关注通知归类到动态
            default:
                return "system";  // 默认归类到系统通知
        }
    }
}
