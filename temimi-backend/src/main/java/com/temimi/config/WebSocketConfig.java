package com.temimi.config;

import com.temimi.handler.ChatWebSocketHandler;
import com.temimi.handler.DanmuWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // ✅ 通过方法引用注册Handler
        registry.addHandler(danmuWebSocketHandler(), "/danmu")
                .setAllowedOrigins("*"); // 允许跨域
        registry.addHandler(chatWebSocketHandler(), "/chat")
                .setAllowedOrigins("*");
    }

    // ✅ 取消注释并添加 @Bean 注解
    @Bean
    public DanmuWebSocketHandler danmuWebSocketHandler() {
        return new DanmuWebSocketHandler();
    }

    // ✅ 取消注释并添加 @Bean 注解
    @Bean
    public ChatWebSocketHandler chatWebSocketHandler() {
        return new ChatWebSocketHandler();
    }
}