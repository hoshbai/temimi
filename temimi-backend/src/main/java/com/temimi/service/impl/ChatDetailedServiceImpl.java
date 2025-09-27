package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.constant.BusinessConstants;
import com.temimi.mapper.ChatDetailedMapper;
import com.temimi.model.entity.Chat;
import com.temimi.model.entity.ChatDetailed;
import com.temimi.service.ChatDetailedService;
import com.temimi.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ChatDetailedServiceImpl extends ServiceImpl<ChatDetailedMapper, ChatDetailed> implements ChatDetailedService {
    
    @Autowired
    private ChatDetailedMapper chatDetailedMapper;
    
    @Autowired
    private ChatService chatService;
    
    @Override
    public Page<ChatDetailed> getChatHistory(Integer userId, Integer anotherId, int pageNum, int pageSize) {
        Page<ChatDetailed> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ChatDetailed> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                    .and(w1 -> w1.eq("user_id", userId).eq("another_id", anotherId).eq("user_del", BusinessConstants.FALSE))
                    .or(w2 -> w2.eq("user_id", anotherId).eq("another_id", userId).eq("another_del", BusinessConstants.FALSE))
                )
                .eq("withdraw", BusinessConstants.FALSE)
                .orderByDesc("time");
                
        return chatDetailedMapper.selectPage(page, queryWrapper);
    }
    
    @Override
    @Transactional
    public boolean sendMessage(Integer userId, Integer anotherId, String content) {
        ChatDetailed message = new ChatDetailed();
        message.setUserId(userId);
        message.setAnotherId(anotherId);
        message.setContent(content);
        message.setUserDel(BusinessConstants.FALSE);
        message.setAnotherDel(BusinessConstants.FALSE);
        message.setWithdraw(BusinessConstants.FALSE);
        message.setTime(LocalDateTime.now());
        
        int result = chatDetailedMapper.insert(message);
        
        if (result > 0) {
            updateChatSessions(userId, anotherId);
        }
        
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean withdrawMessage(Integer messageId, Integer userId) {
        // 检查消息是否存在且属于该用户
        ChatDetailed message = chatDetailedMapper.selectById(messageId);
        if (message == null || !message.getUserId().equals(userId)) {
            return false;
        }
        
        // 检查是否在可撤回时间内
        LocalDateTime now = LocalDateTime.now();
        if (now.minusMinutes(BusinessConstants.MESSAGE_WITHDRAW_TIME_LIMIT).isAfter(message.getTime())) {
            return false;
        }
        
        UpdateWrapper<ChatDetailed> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", messageId)
                   .set("withdraw", BusinessConstants.TRUE);
        return chatDetailedMapper.update(null, updateWrapper) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteMessageBySender(Integer messageId, Integer userId) {
        UpdateWrapper<ChatDetailed> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", messageId)
                   .eq("user_id", userId)
                   .set("user_del", BusinessConstants.TRUE);
        return chatDetailedMapper.update(null, updateWrapper) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteMessageByReceiver(Integer messageId, Integer userId) {
        UpdateWrapper<ChatDetailed> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", messageId)
                   .eq("another_id", userId)
                   .set("another_del", BusinessConstants.TRUE);
        return chatDetailedMapper.update(null, updateWrapper) > 0;
    }

    private void updateChatSessions(Integer userId, Integer anotherId) {
        Chat senderChat = chatService.createOrGetChat(userId, anotherId);
        Chat receiverChat = chatService.createOrGetChat(anotherId, userId);
        
        // 更新发送方的会话时间
        chatService.updateLatestTime(senderChat.getId());
        
        // 更新接收方的会话时间和未读数
        chatService.updateLatestTime(receiverChat.getId());
        chatService.incrementUnreadCount(receiverChat.getId());
    }
}