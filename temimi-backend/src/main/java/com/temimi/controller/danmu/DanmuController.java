package com.temimi.controller.danmu;

import com.temimi.model.entity.Danmu;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.DanmuService;
import com.temimi.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/danmu")
public class DanmuController {

    @Autowired
    private DanmuService danmuService;

    @Autowired
    private com.temimi.handler.DanmuSessionManager danmuSessionManager;

    /**
     * 根据视频ID获取弹幕列表
     * GET /api/danmu/5
     */
    @GetMapping("/{vid}")
    public ApiResult<List<Danmu>> getDanmuList(@PathVariable Integer vid) {
        try {
            List<Danmu> danmuList = danmuService.getDanmuByVid(vid);
            return ApiResult.success(danmuList);
        } catch (Exception e) {
            return ApiResult.error("获取弹幕失败: " + e.getMessage());
        }
    }

    /**
     * 根据视频ID和日期获取历史弹幕
     * GET /api/danmu/history?vid=123&date=2025-09-08
     */
    @GetMapping("/history")
    public ApiResult<List<Danmu>> getDanmuHistory(@RequestParam Integer vid, @RequestParam String date) {
        try {
            // 验证日期格式
            LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<Danmu> danmuList = danmuService.getDanmuByVidAndDate(vid, date);
            return ApiResult.success(danmuList);
        } catch (Exception e) {
            return ApiResult.error("日期格式错误或查询失败: " + e.getMessage());
        }
    }

    /**
     * 发送弹幕
     * POST /api/danmu
     * ✅ 修复：从SecurityUtil获取当前登录用户ID
     */
    @PostMapping
    public ApiResult<String> sendDanmu(@RequestBody Danmu danmu) {
        try {
            // 从JWT token中获取当前用户ID
            Integer uid = SecurityUtil.getCurrentUserIdRequired();

            boolean success = danmuService.sendDanmu(danmu, uid);
            if (success) {
                return ApiResult.success("发送成功");
            } else {
                return ApiResult.error("发送失败");
            }
        } catch (Exception e) {
            return ApiResult.error("发送弹幕失败: " + e.getMessage());
        }
    }

    /**
     * 删除弹幕（DELETE方法）
     * DELETE /api/danmu/{danmuId}
     */
    @DeleteMapping("/{danmuId}")
    public ApiResult<String> deleteDanmu(@PathVariable Integer danmuId) {
        try {
            boolean success = danmuService.deleteDanmu(danmuId);
            if (success) {
                return ApiResult.success("删除成功");
            } else {
                return ApiResult.error("删除失败");
            }
        } catch (Exception e) {
            return ApiResult.error("删除弹幕失败: " + e.getMessage());
        }
    }

    /**
     * 删除弹幕（POST方法，兼容前端）
     * POST /api/danmu/delete
     * 接收表单数据：danmuId=123 或 id=123
     * ✅ 权限验证：只能删除自己发送的弹幕
     */
    @PostMapping("/delete")
    public ApiResult<String> deleteDanmuPost(
            @RequestParam(value = "danmuId", required = false) Integer danmuId,
            @RequestParam(value = "id", required = false) Integer id) {
        try {
            // 优先使用danmuId，如果没有则使用id
            Integer targetId = danmuId != null ? danmuId : id;
            
            if (targetId == null) {
                return ApiResult.error("弹幕ID不能为空");
            }
            
            // 获取当前登录用户ID
            Integer currentUserId = SecurityUtil.getCurrentUserIdRequired();
            
            // 查询弹幕信息
            Danmu danmu = danmuService.getById(targetId);
            if (danmu == null) {
                return ApiResult.error("弹幕不存在");
            }
            
            // ✅ 权限验证：只能删除自己发送的弹幕
            if (!danmu.getUid().equals(currentUserId)) {
                return ApiResult.error("无权删除他人的弹幕");
            }
            
            Integer vid = danmu.getVid();
            
            // 删除弹幕
            boolean success = danmuService.deleteDanmu(targetId);
            if (success) {
                // 通过WebSocket广播删除事件给所有观看该视频的用户
                broadcastDanmuDelete(vid, targetId);
                return ApiResult.success("删除成功");
            } else {
                return ApiResult.error("删除失败");
            }
        } catch (Exception e) {
            return ApiResult.error("删除弹幕失败: " + e.getMessage());
        }
    }

    /**
     * 广播弹幕删除事件
     */
    private void broadcastDanmuDelete(Integer vid, Integer danmuId) {
        try {
            List<org.springframework.web.socket.WebSocketSession> sessions = 
                danmuSessionManager.getSessionsByVid(vid);
            
            // 构造删除消息：{"type": "delete", "danmuId": 123}
            String deleteMessage = String.format("{\"type\":\"delete\",\"danmuId\":%d}", danmuId);
            
            for (org.springframework.web.socket.WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new org.springframework.web.socket.TextMessage(deleteMessage));
                }
            }
        } catch (Exception e) {
            // 广播失败不影响删除操作
            System.err.println("广播弹幕删除事件失败: " + e.getMessage());
        }
    }
}