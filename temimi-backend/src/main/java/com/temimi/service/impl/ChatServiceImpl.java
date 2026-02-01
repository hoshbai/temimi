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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat> implements ChatService {
    
    @Autowired
    private ChatMapper chatMapper;
    
    @Autowired
    private com.temimi.service.MsgUnreadService msgUnreadService;
    
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

    @Autowired
    private com.temimi.service.UserService userService;
    
    @Autowired
    private com.temimi.mapper.ChatDetailedMapper chatDetailedMapper;

    @Override
    public Map<String, Object> getChatListWithUserInfo(Integer userId, Long offset) {
        Map<String, Object> result = new HashMap<>();
        int pageSize = 20;
        
        // 获取聊天列表（分页）
        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("is_deleted", BusinessConstants.FALSE)
                   .orderByDesc("latest_time")
                   .last("LIMIT " + offset + ", " + (pageSize + 1));
        
        List<Chat> chatList = chatMapper.selectList(queryWrapper);
        
        // 判断是否还有更多
        boolean hasMore = chatList.size() > pageSize;
        if (hasMore) {
            chatList = chatList.subList(0, pageSize);
        }
        
        // 构造前端需要的数据格式
        List<Map<String, Object>> resultList = new java.util.ArrayList<>();
        for (Chat chat : chatList) {
            Map<String, Object> item = new HashMap<>();
            
            // 1. chat 信息
            item.put("chat", chat);
            
            // 2. user 信息（对方用户）
            com.temimi.model.entity.User anotherUser = userService.getById(chat.getAnotherId());
            if (anotherUser != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("uid", anotherUser.getUid());
                userInfo.put("nickname", anotherUser.getNickname());
                userInfo.put("avatar_url", anotherUser.getAvatar());
                userInfo.put("auth", anotherUser.getAuth());
                item.put("user", userInfo);
                
                // 3. detail 信息（最后一条消息）
                Map<String, Object> detail = new HashMap<>();
                
                // 直接使用 Mapper 查询，避免循环依赖
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.temimi.model.entity.ChatDetailed> detailQuery = 
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                detailQuery.and(wrapper -> wrapper
                            .and(w1 -> w1.eq("user_id", userId).eq("another_id", chat.getAnotherId()).eq("user_del", BusinessConstants.FALSE))
                            .or(w2 -> w2.eq("user_id", chat.getAnotherId()).eq("another_id", userId).eq("another_del", BusinessConstants.FALSE))
                        )
                        .orderByDesc("time")
                        .last("LIMIT 1");
                
                List<com.temimi.model.entity.ChatDetailed> lastMessages = 
                    chatDetailedMapper.selectList(detailQuery);
                
                if (!lastMessages.isEmpty()) {
                    List<com.temimi.model.entity.ChatDetailed> messageList = new java.util.ArrayList<>();
                    messageList.add(lastMessages.get(0));
                    detail.put("list", messageList);
                } else {
                    detail.put("list", new java.util.ArrayList<>());
                }
                detail.put("more", true);
                item.put("detail", detail);
                
                resultList.add(item);
            }
        }
        
        result.put("list", resultList);
        result.put("more", hasMore);
        return result;
    }

    @Override
    @Transactional
    public boolean deleteChatByAnotherId(Integer userId, Integer anotherId) {
        UpdateWrapper<Chat> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId)
                   .eq("another_id", anotherId)
                   .set("is_deleted", BusinessConstants.TRUE);
        return chatMapper.update(null, updateWrapper) > 0;
    }

    @Override
    @Transactional
    public void updateOnlineStatus(Integer userId, Integer fromUid) {
        // 查找聊天会话
        QueryWrapper<Chat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("another_id", fromUid);
        Chat chat = chatMapper.selectOne(queryWrapper);

        if (chat != null && chat.getUnread() > 0) {
            // 获取当前未读数
            int unreadCount = chat.getUnread();
            
            // 清除会话未读消息数
            clearUnreadCount(chat.getId());
            
            // ✅ 减少全局私信未读数
            msgUnreadService.decrementUnreadCount(userId, "whisper", unreadCount);
        }
    }

    @Override
    @Transactional
    public void updateOutlineStatus(Integer fromUid, Integer toUid) {
        // 更新离线状态的逻辑
        // 可以在这里记录用户离线时间或其他相关操作
    }
}