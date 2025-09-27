package com.temimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.temimi.constant.BusinessConstants;
import com.temimi.mapper.ChatMapper;
import com.temimi.model.entity.Chat;
import com.temimi.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat> implements ChatService {
    
    @Autowired
    private ChatMapper chatMapper;
    
    @Override
    public List<Chat> getChatListByUserId(Integer userId) {
        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("is_deleted", BusinessConstants.FALSE)
                   .orderByDesc("latest_time");
        return chatMapper.selectList(queryWrapper);
    }
    
    @Override
    @Transactional
    public Chat createOrGetChat(Integer userId, Integer anotherId) {
        // 查找是否已存在聊天会话
        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("another_id", anotherId);
        
        Chat existingChat = chatMapper.selectOne(queryWrapper);
        if (existingChat != null) {
            return existingChat;
        }
        
        // 创建新的聊天会话
        Chat newChat = new Chat();
        newChat.setUserId(userId);
        newChat.setAnotherId(anotherId);
        newChat.setIsDeleted(BusinessConstants.FALSE);
        newChat.setUnread(0);
        newChat.setLatestTime(LocalDateTime.now());
        
        chatMapper.insert(newChat);
        return newChat;
    }
    
    @Override
    @Transactional
    public boolean updateLatestTime(Integer chatId) {
        UpdateWrapper<Chat> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", chatId)
                   .set("latest_time", LocalDateTime.now());
        return chatMapper.update(null, updateWrapper) > 0;
    }
    
    @Override
    @Transactional
    public boolean incrementUnreadCount(Integer chatId) {
        UpdateWrapper<Chat> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", chatId)
                   .setSql("unread = unread + 1");
        return chatMapper.update(null, updateWrapper) > 0;
    }
    
    @Override
    @Transactional
    public boolean clearUnreadCount(Integer chatId) {
        UpdateWrapper<Chat> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", chatId)
                   .set("unread", 0);
        return chatMapper.update(null, updateWrapper) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteChat(Integer chatId, Integer userId) {
        UpdateWrapper<Chat> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", chatId)
                   .eq("user_id", userId)
                   .set("is_deleted", BusinessConstants.TRUE);
        return chatMapper.update(null, updateWrapper) > 0;
    }
}