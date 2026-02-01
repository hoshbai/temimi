package com.temimi.config;

import com.temimi.handler.ChatWebSocketHandler;
import com.temimi.handler.DanmuWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private DanmuWebSocketHandler danmuWebSocketHandler;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 弹幕 WebSocket 端点 - 支持路径参数 /ws/danmu/{vid}
        registry.addHandler(danmuWebSocketHandler, "/ws/danmu/**")
                .setAllowedOrigins("*"); // 允许跨域
        
        // 聊天 WebSocket 端点
        registry.addHandler(chatWebSocketHandler, "/chat")
                .setAllowedOrigins("*");
        
        // 即时通讯 WebSocket 端点
        registry.addHandler(chatWebSocketHandler, "/im")
                .setAllowedOrigins("*");
    }
}