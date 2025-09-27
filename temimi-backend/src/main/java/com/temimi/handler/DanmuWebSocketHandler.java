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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String vidParam = session.getUri().getQuery();
        if (vidParam != null && vidParam.startsWith("vid=")) {
            try {
                Integer vid = Integer.valueOf(vidParam.split("=")[1]);
                session.getAttributes().put("vid", vid);
                danmuSessionManager.bindSession(vid, session);
                logger.info("用户加入视频 {} 的弹幕池", vid);
            } catch (NumberFormatException e) {
                logger.error("无效的视频ID: {}", vidParam);
                session.close(CloseStatus.BAD_DATA);
            }
        } else {
            logger.error("未提供视频ID，连接将被关闭");
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.info("收到弹幕消息: {}", payload);

        try {
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
        } catch (Exception e) {
            logger.error("解析弹幕消息失败: {}", e.getMessage());
            sendErrorResponse(session, "消息格式错误");
        }
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
        }
    }
}