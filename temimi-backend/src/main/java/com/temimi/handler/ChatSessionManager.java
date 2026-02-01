package com.temimi.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(ChatSessionManager.class);

    private final Map<Integer, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    public void bindSession(Integer uid, WebSocketSession session) {
        // 如果用户已经有session，先关闭旧的
        WebSocketSession oldSession = sessionMap.get(uid);
        if (oldSession != null && oldSession.isOpen()) {
            try {
                logger.info("用户 {} 重复登录，关闭旧Session: {}", uid, oldSession.getId());
                oldSession.close();
            } catch (Exception e) {
                logger.error("关闭旧Session失败: {}", e.getMessage());
            }
        }
        
        sessionMap.put(uid, session);
        logger.info("用户 {} 上线，SessionID: {}, 当前在线用户数: {}", uid, session.getId(), sessionMap.size());
    }

    public void unbindSession(Integer uid) {
        WebSocketSession session = sessionMap.remove(uid);
        if (session != null) {
            logger.info("用户 {} 下线，SessionID: {}, 当前在线用户数: {}", uid, session.getId(), sessionMap.size());
        }
    }

    public WebSocketSession getSessionByUid(Integer uid) {
        WebSocketSession session = sessionMap.get(uid);
        // 如果session已关闭，从map中移除
        if (session != null && !session.isOpen()) {
            logger.warn("用户 {} 的Session已关闭，从在线列表中移除", uid);
            sessionMap.remove(uid);
            return null;
        }
        return session;
    }

    public boolean isUserOnline(Integer uid) {
        WebSocketSession session = sessionMap.get(uid);
        boolean isOnline = session != null && session.isOpen();
        if (session != null && !session.isOpen()) {
            // 清理已关闭的session
            sessionMap.remove(uid);
        }
        return isOnline;
    }
    
    public int getOnlineUserCount() {
        // 清理所有已关闭的session
        sessionMap.entrySet().removeIf(entry -> !entry.getValue().isOpen());
        return sessionMap.size();
    }
    
    public java.util.Set<Integer> getOnlineUserIds() {
        // 清理所有已关闭的session
        sessionMap.entrySet().removeIf(entry -> !entry.getValue().isOpen());
        return sessionMap.keySet();
    }
}