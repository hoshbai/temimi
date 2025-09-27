package com.temimi.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DanmuSessionManager {
    // vid -> List<Session>
    private final Map<Integer, List<WebSocketSession>> sessionMap = new ConcurrentHashMap<>();

    public void bindSession(Integer vid, WebSocketSession session) {
        sessionMap.computeIfAbsent(vid, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    public void unbindSession(Integer vid, WebSocketSession session) {
        List<WebSocketSession> sessions = sessionMap.get(vid);
        if (sessions != null) {
            sessions.remove(session);
        }
    }

    public List<WebSocketSession> getSessionsByVid(Integer vid) {
        return sessionMap.getOrDefault(vid, List.of());
    }
}