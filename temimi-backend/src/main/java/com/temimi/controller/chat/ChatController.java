package com.temimi.controller.chat;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.entity.Chat;
import com.temimi.model.entity.ChatDetailed;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.ChatService;
import com.temimi.service.ChatDetailedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private ChatDetailedService chatDetailedService;
    
    /**
     * 获取用户聊天列表
     */
    @GetMapping("/list")
    public ApiResult<List<Chat>> getChatList(@RequestHeader("uid") Integer userId) {
        List<Chat> chatList = chatService.getChatListByUserId(userId);
        return ApiResult.success(chatList);
    }
    
    /**
     * 获取聊天记录
     */
    @GetMapping("/history")
    public ApiResult<Page<ChatDetailed>> getChatHistory(
            @RequestHeader("uid") Integer userId,
            @RequestParam Integer anotherId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<ChatDetailed> history = chatDetailedService.getChatHistory(userId, anotherId, pageNum, pageSize);
        return ApiResult.success(history);
    }
    
    /**
     * 发送消息
     */
    @PostMapping("/send")
    public ApiResult<String> sendMessage(
            @RequestHeader("uid") Integer userId,
            @RequestParam Integer anotherId,
            @RequestParam String content) {
        boolean success = chatDetailedService.sendMessage(userId, anotherId, content);
        return success ? ApiResult.success("发送成功") : ApiResult.error("发送失败");
    }
    
    /**
     * 撤回消息
     */
    @PostMapping("/withdraw/{messageId}")
    public ApiResult<String> withdrawMessage(
            @PathVariable Integer messageId,
            @RequestHeader("uid") Integer userId) {
        boolean success = chatDetailedService.withdrawMessage(messageId, userId);
        return success ? ApiResult.success("撤回成功") : ApiResult.error("撤回失败");
    }
    
    /**
     * 清除未读消息
     */
    @PostMapping("/clearUnread/{chatId}")
    public ApiResult<String> clearUnread(
            @PathVariable Integer chatId,
            @RequestHeader("uid") Integer userId) {
        boolean success = chatService.clearUnreadCount(chatId);
        return success ? ApiResult.success("已清除") : ApiResult.error("清除失败");
    }
}