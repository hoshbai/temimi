package com.temimi.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.temimi.constant.CommandType;
import com.temimi.model.entity.Command;
import com.temimi.model.dto.ChatMessageDTO;
import com.temimi.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ChatSessionManager chatSessionManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = session.getHandshakeHeaders().getFirst("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Integer uid = jwtUtil.getUserIdFromToken(token);
                session.getAttributes().put("uid", uid);
                chatSessionManager.bindSession(uid, session);
                logger.info("用户 {} 连接聊天WebSocket", uid);
                
                // 发送连接确认
                sendConnectionAck(session);
            } catch (Exception e) {
                logger.error("无效的Token，连接将被关闭: {}", e.getMessage());
                session.close(CloseStatus.BAD_DATA);
            }
        } else {
            logger.error("未提供Token，连接将被关闭");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.info("收到WebSocket消息: {}", payload);

        try {
            Command command = objectMapper.readValue(payload, Command.class);
            CommandType commandType = CommandType.match(command.getCode());
            
            if (commandType == CommandType.ERROR) {
                sendErrorResponse(session, "无效的命令类型: " + command.getCode());
                return;
            }
            
            switch (commandType) {
                case CHAT_SEND:
                    handleChatMessage(command.getContent(), session);
                    break;
                case HEARTBEAT:
                    handleHeartbeat(session);
                    break;
                case CHAT_WITHDRAW:
                    handleChatWithdraw(command.getContent(), session);
                    break;
                default:
                    logger.warn("未处理的消息类型: {}", commandType);
                    break;
            }
        } catch (Exception e) {
            logger.error("解析WebSocket消息失败: {}", e.getMessage());
            sendErrorResponse(session, "消息格式错误");
        }
    }

    private void sendConnectionAck(WebSocketSession session) throws Exception {
        Command ackCommand = Command.success(CommandType.CONNECTION, "连接成功");
        String ackJson = objectMapper.writeValueAsString(ackCommand);
        session.sendMessage(new TextMessage(ackJson));
    }

    private void handleChatMessage(String content, WebSocketSession session) throws Exception {
        ChatMessageDTO messageDTO = objectMapper.readValue(content, ChatMessageDTO.class);
        Integer toUid = messageDTO.getToUid();
        String messageContent = messageDTO.getContent();

        WebSocketSession targetSession = chatSessionManager.getSessionByUid(toUid);
        if (targetSession != null && targetSession.isOpen()) {
            Command responseCommand = Command.success(CommandType.CHAT_SEND, messageContent);
            String responseJson = objectMapper.writeValueAsString(responseCommand);
            targetSession.sendMessage(new TextMessage(responseJson));
            logger.info("消息已发送给用户 {}", toUid);
        } else {
            logger.warn("目标用户 {} 不在线或会话已关闭", toUid);
        }
    }

    private void handleHeartbeat(WebSocketSession session) throws Exception {
        Command heartbeatResponse = Command.heartbeatResponse();
        String responseJson = objectMapper.writeValueAsString(heartbeatResponse);
        session.sendMessage(new TextMessage(responseJson));
        logger.debug("发送心跳响应");
    }
    
    private void handleChatWithdraw(String content, WebSocketSession session) throws Exception {
        logger.info("处理消息撤回: {}", content);
        // TODO: 实现消息撤回功能
    }

    private void sendErrorResponse(WebSocketSession session, String errorMessage) {
        try {
            Command errorCommand = Command.error(errorMessage);
            String errorJson = objectMapper.writeValueAsString(errorCommand);
            session.sendMessage(new TextMessage(errorJson));
        } catch (Exception e) {
            logger.error("发送错误响应失败: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Integer uid = (Integer) session.getAttributes().get("uid");
        if (uid != null) {
            chatSessionManager.unbindSession(uid);
            logger.info("用户 {} 断开聊天WebSocket连接", uid);
        }
    }
}