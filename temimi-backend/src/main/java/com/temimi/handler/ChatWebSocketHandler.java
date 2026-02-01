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

import java.util.HashMap;
import java.util.Map;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ChatSessionManager chatSessionManager;

    @Autowired
    private com.temimi.service.ChatDetailedService chatDetailedService;

    @Autowired
    private com.temimi.service.ChatService chatService;

    @Autowired
    private com.temimi.service.UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = null;
        Integer targetUid = null;
        
        // 优先从 Authorization header 获取 token
        String authHeader = session.getHandshakeHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        
        // 如果 header 中没有，尝试从 URL 参数获取
        if (token == null) {
            String query = session.getUri().getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("token=")) {
                        token = param.substring(6);
                    } else if (param.startsWith("targetUid=") || param.startsWith("anotherId=")) {
                        try {
                            String[] parts = param.split("=");
                            if (parts.length > 1) {
                                targetUid = Integer.parseInt(parts[1]);
                            }
                        } catch (NumberFormatException e) {
                            logger.warn("无法解析目标用户ID: {}", param);
                        }
                    }
                }
            }
        }
        
        if (token != null) {
            try {
                Integer uid = jwtUtil.getUserIdFromToken(token);
                session.getAttributes().put("uid", uid);
                
                // 保存目标用户ID到session
                if (targetUid != null) {
                    session.getAttributes().put("targetUid", targetUid);
                    logger.info("用户 {} 连接聊天WebSocket，目标用户: {}, SessionID: {}", uid, targetUid, session.getId());
                } else {
                    logger.info("用户 {} 连接聊天WebSocket, SessionID: {}", uid, session.getId());
                }
                
                chatSessionManager.bindSession(uid, session);
                
                // 发送连接确认
                sendConnectionAck(session);
                
                // 打印当前在线用户列表（用于调试）
                logger.info("当前在线用户数: {}", chatSessionManager.getOnlineUserCount());
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
                    handleChatMessage(command, session);
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

    private void handleChatMessage(Command command, WebSocketSession session) throws Exception {
        Integer fromUid = (Integer) session.getAttributes().get("uid");
        String content = command.getContent();
        Integer toUid = command.getAnotherId();  // 从Command对象获取目标用户ID
        
        // 如果anotherId为null，尝试从session中获取
        if (toUid == null) {
            toUid = (Integer) session.getAttributes().get("targetUid");
            logger.debug("从session中获取目标用户ID: {}", toUid);
        }
        
        // 如果还是null，尝试从content中解析
        if (toUid == null) {
            try {
                ChatMessageDTO messageDTO = objectMapper.readValue(content, ChatMessageDTO.class);
                toUid = messageDTO.getToUid();
                content = messageDTO.getContent();
                logger.debug("从消息内容中解析目标用户ID: {}", toUid);
            } catch (Exception e) {
                logger.warn("无法确定目标用户ID，消息格式可能不正确");
                sendErrorResponse(session, "消息格式错误：缺少目标用户ID。请在URL中添加 targetUid 参数，例如: ws://localhost:8080/chat?token=xxx&targetUid=123");
                return;
            }
        }

        if (toUid == null) {
            sendErrorResponse(session, "消息格式错误：缺少目标用户ID");
            return;
        }

        // 1. 先保存消息到数据库（无论对方是否在线）
        try {
            chatDetailedService.sendMessage(fromUid, toUid, content);
            logger.info("消息已保存到数据库：从用户 {} 发送给用户 {}", fromUid, toUid);
        } catch (Exception e) {
            logger.error("保存消息到数据库失败: {}", e.getMessage());
            sendErrorResponse(session, "消息发送失败");
            return;
        }

        // 2. 构造前端期望的消息格式
        // 前端期望格式：{ type: 'whisper', data: { type: '接收', detail: {...}, chat: {...}, user: {...} } }
        
        // 获取或创建聊天会话
        com.temimi.model.entity.Chat chat = chatService.createOrGetChat(fromUid, toUid);
        
        // 获取发送者和接收者用户信息
        com.temimi.model.entity.User fromUser = userService.getById(fromUid);
        com.temimi.model.entity.User toUser = userService.getById(toUid);
        
        // 构造 detail 对象（发送者和接收者共用）
        Map<String, Object> detail = new HashMap<>();
        detail.put("userId", fromUid);
        detail.put("anotherId", toUid);
        detail.put("content", content);
        detail.put("createTime", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        
        // 构造 chat 对象（发送者和接收者共用）
        Map<String, Object> chatMap = new HashMap<>();
        chatMap.put("id", chat.getId());
        chatMap.put("userId", chat.getUserId());
        chatMap.put("anotherId", chat.getAnotherId());
        chatMap.put("latestTime", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        chatMap.put("unread", 0);
        
        // 3. 给接收者发送消息（user 是发送者信息）
        WebSocketSession targetSession = chatSessionManager.getSessionByUid(toUid);
        logger.info("尝试发送消息给用户 {}, Session存在: {}, Session打开: {}", 
            toUid, 
            targetSession != null, 
            targetSession != null && targetSession.isOpen());
        
        if (targetSession != null && targetSession.isOpen()) {
            Map<String, Object> fromUserMap = new HashMap<>();
            fromUserMap.put("uid", fromUser.getUid());
            fromUserMap.put("nickname", fromUser.getNickname());
            fromUserMap.put("avatar_url", fromUser.getAvatar());
            fromUserMap.put("auth", fromUser.getAuth());
            
            Map<String, Object> toWhisperData = new HashMap<>();
            toWhisperData.put("type", "接收");
            toWhisperData.put("detail", detail);
            toWhisperData.put("chat", chatMap);
            toWhisperData.put("user", fromUserMap);  // 接收者看到的是发送者信息
            toWhisperData.put("online", true);
            
            Map<String, Object> toResponse = new HashMap<>();
            toResponse.put("type", "whisper");
            toResponse.put("data", toWhisperData);
            
            String toResponseJson = objectMapper.writeValueAsString(toResponse);
            targetSession.sendMessage(new TextMessage(toResponseJson));
            logger.info("✓ 消息已实时推送给在线用户 {}, SessionID: {}", toUid, targetSession.getId());
        } else {
            logger.warn("✗ 目标用户 {} 不在线或Session已关闭，消息已保存为离线消息。在线用户列表: {}", 
                toUid, 
                chatSessionManager.getOnlineUserIds());
        }
        
        // 4. 给发送者发送确认（user 是接收者信息）
        Map<String, Object> toUserMap = new HashMap<>();
        toUserMap.put("uid", toUser.getUid());
        toUserMap.put("nickname", toUser.getNickname());
        toUserMap.put("avatar_url", toUser.getAvatar());
        toUserMap.put("auth", toUser.getAuth());
        
        Map<String, Object> fromWhisperData = new HashMap<>();
        fromWhisperData.put("type", "接收");
        fromWhisperData.put("detail", detail);
        fromWhisperData.put("chat", chatMap);
        fromWhisperData.put("user", toUserMap);  // 发送者看到的是接收者信息
        fromWhisperData.put("online", true);
        
        Map<String, Object> fromResponse = new HashMap<>();
        fromResponse.put("type", "whisper");
        fromResponse.put("data", fromWhisperData);
        
        String fromResponseJson = objectMapper.writeValueAsString(fromResponse);
        session.sendMessage(new TextMessage(fromResponseJson));
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