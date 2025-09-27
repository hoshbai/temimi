package com.temimi.controller.danmu;

import com.temimi.model.entity.Danmu;
import com.temimi.service.DanmuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * 弹幕WebSocket控制器
 */
@Controller
public class DanmuWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(DanmuWebSocketController.class);

    @Autowired
    private DanmuService danmuService;

    /**
     * 处理弹幕发送
     * 客户端发送到: /app/danmu/send
     * 服务端广播到: /topic/danmu/{vid}
     */
    @MessageMapping("/danmu/send")
    @SendTo("/topic/danmu/{vid}")
    public Danmu sendDanmu(Danmu danmu) {
        try {
            // 调用service层处理弹幕发送逻辑
            boolean success = danmuService.sendDanmu(danmu, danmu.getUid());
            if (success) {
                return danmu;
            }
            return null;
        } catch (Exception e) {
            logger.error("弹幕发送失败: {}", e.getMessage(), e);
            return null;
        }
    }
}