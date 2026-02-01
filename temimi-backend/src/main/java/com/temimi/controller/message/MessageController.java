package com.temimi.controller.message;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.entity.Message;
import com.temimi.model.entity.MsgUnread;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.MessageService;
import com.temimi.service.MsgUnreadService;
import com.temimi.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 * 处理系统消息（回复、点赞、系统通知、关注等）
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MsgUnreadService msgUnreadService;

    /**
     * 获取消息列表（分页）
     * GET /api/message/list?type=reply&pageNum=1&pageSize=20
     *
     * 消息类型：
     * - reply: 回复我的
     * - like: 点赞我的
     * - system: 系统通知
     * - follow: 关注通知
     */
    @GetMapping("/list")
    public ApiResult<Page<Message>> getMessages(
            @RequestParam String type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            Page<Message> messages = messageService.getMessagesByType(uid, type, pageNum, pageSize);
            return ApiResult.success(messages);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     * POST /api/message/mark-read
     *
     * 请求体示例：
     * {
     *   "type": "reply",
     *   "messageIds": [1, 2, 3]  // 可选，如果为空则标记该类型所有消息为已读
     * }
     */
    @PostMapping("/mark-read")
    public ApiResult<String> markAsRead(@RequestBody Map<String, Object> params) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            String type = (String) params.get("type");

            @SuppressWarnings("unchecked")
            List<Integer> messageIds = (List<Integer>) params.get("messageIds");

            boolean success = messageService.markAsRead(uid, type, messageIds);
            if (success) {
                return ApiResult.success("标记成功");
            } else {
                return ApiResult.error("标记失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 创建新消息（系统内部使用）
     * POST /api/message/create
     *
     * 注意：此接口主要供系统内部调用，用于生成消息通知
     * 例如：用户点赞视频时，系统自动创建一条"点赞"消息发送给视频作者
     */
    @PostMapping("/create")
    public ApiResult<String> createMessage(@RequestBody Message message) {
        try {
            boolean success = messageService.createMessage(message);
            if (success) {
                return ApiResult.success("消息创建成功");
            } else {
                return ApiResult.error("消息创建失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 删除消息
     * DELETE /api/message/delete/{messageId}
     */
    @DeleteMapping("/delete/{messageId}")
    public ApiResult<String> deleteMessage(@PathVariable Integer messageId) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean success = messageService.deleteMessage(uid, messageId);
            if (success) {
                return ApiResult.success("删除成功");
            } else {
                return ApiResult.error("删除失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取所有类型的未读消息数
     * GET /api/message/unread-counts
     *
     * 返回数据格式：
     * {
     *   "reply": 5,
     *   "at": 2,
     *   "love": 10,
     *   "system": 1,
     *   "whisper": 3,
     *   "dynamic": 0
     * }
     */
    @GetMapping("/unread-counts")
    public ApiResult<MsgUnread> getAllUnreadCounts() {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            MsgUnread msgUnread = msgUnreadService.getUnreadCountByUid(uid);
            return ApiResult.success(msgUnread);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取指定类型的未读消息数
     * GET /api/message/unread-count?type=reply
     */
    @GetMapping("/unread-count")
    public ApiResult<Integer> getUnreadCountByType(@RequestParam String type) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            Integer count = messageService.getUnreadCountByType(uid, type);
            return ApiResult.success(count);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 清除某类型的未读数（前端调用）
     * POST /api/msg-unread/clear?category=reply
     */
    @PostMapping("/msg-unread/clear")
    public ApiResult<String> clearUnread(@RequestParam String category) {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            boolean success = msgUnreadService.clearUnreadCount(uid, category);
            if (success) {
                return ApiResult.success("已清除未读数");
            } else {
                return ApiResult.error("清除失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}
