package com.temimi.controller.system;

import com.temimi.model.entity.MsgUnread;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.MsgUnreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/msg-unread")
public class MsgUnreadController {

    @Autowired
    private MsgUnreadService msgUnreadService;

    /**
     * 获取用户的所有未读消息数
     * GET /api/msg-unread/all
     */
    @GetMapping("/all")
    public ApiResult<MsgUnread> getUnreadCount(@RequestAttribute("uid") Integer uid) {
        try {
            MsgUnread msgUnread = msgUnreadService.getUnreadCountByUid(uid);
            if (msgUnread == null) {
                return ApiResult.error("用户不存在");
            }
            return ApiResult.success(msgUnread);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 清除指定类型的未读数
     * POST /api/msg-unread/clear?category=reply
     */
    @PostMapping("/clear")
    public ApiResult<String> clearUnreadCount(@RequestParam String category) {
        try {
            Integer uid = com.temimi.util.SecurityUtil.getCurrentUserIdRequired();
            boolean success = msgUnreadService.clearUnreadCount(uid, category);
            if (success) {
                return ApiResult.success("清除成功");
            } else {
                return ApiResult.error("清除失败");
            }
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }
}