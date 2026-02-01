package com.temimi.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.temimi.constant.CommandType;
import com.temimi.model.entity.Command;
import com.temimi.model.entity.Danmu;
import com.temimi.model.dto.DanmuMessageDTO;
import com.temimi.service.DanmuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

@Component
public class DanmuWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(DanmuWebSocketHandler.class);

    @Autowired
    private DanmuService danmuService;

    @Autowired
    private DanmuSessionManager danmuSessionManager;

    @Autowired
    private com.temimi.util.JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules(); // 自动注册所有可用模块，包括JSR310

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从路径中提取视频ID: /ws/danmu/{vid}
        String path = session.getUri().getPath();
        logger.info("弹幕WebSocket连接建立，路径: {}", path);
        
        try {
            // 提取路径中的最后一段作为视频ID
            String[] pathParts = path.split("/");
            String vidStr = pathParts[pathParts.length - 1];
            Integer vid = Integer.valueOf(vidStr);
            
            session.getAttributes().put("vid", vid);
            danmuSessionManager.bindSession(vid, session);
            logger.info("用户加入视频 {} 的弹幕池", vid);
            
            // 广播当前观看人数给所有观看该视频的用户
            broadcastPopulation(vid);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            logger.error("无效的视频ID，路径: {}", path);
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.info("收到弹幕消息: {}", payload);

        try {
            // ✅ 修复：兼容前端发送的格式 {token: "xxx", data: {...}}
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(payload);
            
            // 检查是否是前端格式（包含token和data字段）
            if (rootNode.has("token") && rootNode.has("data")) {
                // 前端格式：{token: "Bearer xxx", data: {content, time, mode, color}}
                String token = rootNode.get("token").asText();
                String dataJson = rootNode.get("data").toString();
                
                // 从token中提取用户ID
                Integer uid = extractUidFromToken(token);
                if (uid == null) {
                    sendErrorResponse(session, "无效的token或用户未登录");
                    return;
                }
                
                // 保存用户ID到session
                session.getAttributes().put("uid", uid);
                
                // 处理弹幕消息
                handleDanmuMessageFromFrontend(dataJson, session, uid);
            } else {
                // 后端Command格式：{code: xxx, content: "..."}
                Command command = objectMapper.readValue(payload, Command.class);
                CommandType commandType = CommandType.match(command.getCode());
                
                if (commandType == CommandType.ERROR) {
                    sendErrorResponse(session, "无效的命令类型: " + command.getCode());
                    return;
                }
                
                switch (commandType) {
                    case DANMU_SEND:
                        handleDanmuMessage(command.getContent(), session);
                        break;
                    case HEARTBEAT:
                        handleHeartbeat(session);
                        break;
                    default:
                        logger.warn("未处理的弹幕消息类型: {}", commandType);
                        break;
                }
            }
        } catch (Exception e) {
            logger.error("解析弹幕消息失败: {}", e.getMessage(), e);
            sendErrorResponse(session, "消息格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 从JWT Token中提取用户ID
     */
    private Integer extractUidFromToken(String token) {
        try {
            // 移除 "Bearer " 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // 使用JWT工具类解析token
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            logger.error("解析token失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 处理前端格式的弹幕消息
     */
    private void handleDanmuMessageFromFrontend(String dataJson, WebSocketSession session, Integer uid) throws Exception {
        DanmuMessageDTO danmuDTO = objectMapper.readValue(dataJson, DanmuMessageDTO.class);
        Integer vid = (Integer) session.getAttributes().get("vid");
        
        if (vid == null) {
            logger.error("会话中未找到视频ID");
            sendErrorResponse(session, "视频ID不存在");
            return;
        }

        // 创建弹幕对象
        Danmu danmu = new Danmu();
        danmu.setContent(danmuDTO.getContent());
        danmu.setVid(vid);
        danmu.setTimePoint(danmuDTO.getTime());
        danmu.setMode(danmuDTO.getMode());
        danmu.setColor(danmuDTO.getColor());
        danmu.setFontsize(danmuDTO.getFontsize() != null ? danmuDTO.getFontsize() : 25);  // ✅ 设置字体大小
        danmu.setUid(uid);  // ✅ 使用从token中提取的用户ID

        // 保存弹幕到数据库
        boolean saved = danmuService.sendDanmu(danmu, uid);
        if (!saved) {
            logger.error("保存弹幕到数据库失败");
            sendErrorResponse(session, "弹幕发送失败");
            return;
        }

        logger.info("弹幕保存成功: vid={}, uid={}, content={}", vid, uid, danmuDTO.getContent());

        // 广播弹幕给所有观看该视频的用户（返回完整的弹幕对象）
        List<WebSocketSession> targetSessions = danmuSessionManager.getSessionsByVid(vid);
        String broadcastJson = objectMapper.writeValueAsString(danmu);

        for (WebSocketSession targetSession : targetSessions) {
            if (targetSession.isOpen()) {
                targetSession.sendMessage(new TextMessage(broadcastJson));
            }
        }
        
        logger.info("弹幕已广播给 {} 个用户", targetSessions.size());
    }

    private void handleDanmuMessage(String content, WebSocketSession session) throws Exception {
        DanmuMessageDTO danmuDTO = objectMapper.readValue(content, DanmuMessageDTO.class);
        Integer vid = (Integer) session.getAttributes().get("vid");
        
        if (vid == null) {
            logger.error("会话中未找到视频ID");
            return;
        }

        // 创建弹幕对象
        Danmu danmu = new Danmu();
        danmu.setContent(danmuDTO.getContent());
        danmu.setVid(vid);
        danmu.setTimePoint(danmuDTO.getTime());
        danmu.setMode(danmuDTO.getMode());
        danmu.setColor(danmuDTO.getColor());
        // TODO: 从JWT Token中获取用户ID
        danmu.setUid(1); // 临时设置

        // 保存弹幕到数据库
        boolean saved = danmuService.sendDanmu(danmu, danmu.getUid());
        if (!saved) {
            logger.error("保存弹幕到数据库失败");
            return;
        }

        // 广播弹幕给所有观看该视频的用户
        List<WebSocketSession> targetSessions = danmuSessionManager.getSessionsByVid(vid);
        Command broadcastCommand = Command.success(CommandType.DANMU_SEND, content);
        String broadcastJson = objectMapper.writeValueAsString(broadcastCommand);

        for (WebSocketSession targetSession : targetSessions) {
            if (targetSession.isOpen()) {
                targetSession.sendMessage(new TextMessage(broadcastJson));
            }
        }
        
        logger.info("弹幕已广播给 {} 个用户", targetSessions.size());
    }

    private void handleHeartbeat(WebSocketSession session) throws Exception {
        Command heartbeatResponse = Command.heartbeatResponse();
        String responseJson = objectMapper.writeValueAsString(heartbeatResponse);
        session.sendMessage(new TextMessage(responseJson));
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
        Integer vid = (Integer) session.getAttributes().get("vid");
        if (vid != null) {
            danmuSessionManager.unbindSession(vid, session);
            logger.info("用户离开视频 {} 的弹幕池", vid);
            
            // 广播当前观看人数给所有观看该视频的用户
            broadcastPopulation(vid);
        }
    }
    
    /**
     * 广播当前观看人数
     */
    private void broadcastPopulation(Integer vid) {
        List<WebSocketSession> sessions = danmuSessionManager.getSessionsByVid(vid);
        int population = sessions.size();
        String message = "当前观看人数: " + population;
        
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    logger.error("发送观看人数失败: {}", e.getMessage());
                }
            }
        }
        
        logger.info("视频 {} 当前观看人数: {}", vid, population);
    }
}