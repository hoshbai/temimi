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
        sessionMap.put(uid, session);
        logger.info("用户 {} 上线，当前在线用户数: {}", uid, sessionMap.size());
    }

    public void unbindSession(Integer uid) {
        sessionMap.remove(uid);
        logger.info("用户 {} 下线，当前在线用户数: {}", uid, sessionMap.size());
    }

    public WebSocketSession getSessionByUid(Integer uid) {
        return sessionMap.get(uid);
    }

    public boolean isUserOnline(Integer uid) {
        WebSocketSession session = sessionMap.get(uid);
        return session != null && session.isOpen();
    }
}