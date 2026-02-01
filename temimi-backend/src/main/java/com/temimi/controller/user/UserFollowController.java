package com.temimi.controller.user;

import com.temimi.model.dto.UserDto;
import com.temimi.service.UserFollowService;
import com.temimi.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户关注Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/user/follow")
public class UserFollowController {

    @Autowired
    private UserFollowService userFollowService;

    /**
     * 关注/取消关注用户
     * @param followingId 被关注用户ID
     * @param action 操作类型 follow-关注 unfollow-取消关注
     * @return 操作结果
     */
    @PostMapping("/{followingId}")
    public ResponseEntity<Map<String, Object>> followUser(
            @PathVariable Integer followingId,
            @RequestParam String action) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 从 SecurityContext 获取当前用户ID
            Integer currentUserId = SecurityUtil.getCurrentUserIdRequired();

            boolean isFollow = "follow".equals(action);
            boolean result = userFollowService.followUser(currentUserId, followingId, isFollow);

            if (result) {
                response.put("code", 200);
                response.put("message", isFollow ? "关注成功" : "取消关注成功");
                response.put("data", Map.of("isFollowing", isFollow));
            } else {
                response.put("code", 500);
                response.put("message", "操作失败");
            }
        } catch (Exception e) {
            log.error("关注/取消关注失败", e);
            response.put("code", 500);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 检查是否已关注某用户
     * @param followingId 被关注用户ID
     * @return 关注状态
     */
    @GetMapping("/status/{followingId}")
    public ResponseEntity<Map<String, Object>> checkFollowStatus(
            @PathVariable Integer followingId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 从 SecurityContext 获取当前用户ID
            Integer currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId == null) {
                // 未登录用户，返回未关注状态
                response.put("code", 200);
                response.put("message", "未登录");
                response.put("data", Map.of("isFollowing", false));
                return ResponseEntity.ok(response);
            }

            boolean isFollowing = userFollowService.isFollowing(currentUserId, followingId);

            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", Map.of("isFollowing", isFollowing));
        } catch (Exception e) {
            log.error("查询关注状态失败", e);
            response.put("code", 500);
            response.put("message", "查询失败");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户的粉丝列表
     * @param uid 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 粉丝列表
     */
    @GetMapping("/fans/{uid}")
    public ResponseEntity<Map<String, Object>> getFansList(
            @PathVariable Integer uid,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<UserDto> fansList = userFollowService.getFansList(uid, page, pageSize);
            Integer totalCount = userFollowService.getFansCount(uid);

            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", Map.of(
                    "list", fansList,
                    "total", totalCount,
                    "page", page,
                    "pageSize", pageSize
            ));
        } catch (Exception e) {
            log.error("获取粉丝列表失败", e);
            response.put("code", 500);
            response.put("message", "查询失败");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户的关注列表
     * @param uid 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 关注列表
     */
    @GetMapping("/following/{uid}")
    public ResponseEntity<Map<String, Object>> getFollowingList(
            @PathVariable Integer uid,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<UserDto> followingList = userFollowService.getFollowingList(uid, page, pageSize);
            Integer totalCount = userFollowService.getFollowingCount(uid);

            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", Map.of(
                    "list", followingList,
                    "total", totalCount,
                    "page", page,
                    "pageSize", pageSize
            ));
        } catch (Exception e) {
            log.error("获取关注列表失败", e);
            response.put("code", 500);
            response.put("message", "查询失败");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户的粉丝和关注数统计
     * @param uid 用户ID
     * @return 统计数据
     */
    @GetMapping("/stats/{uid}")
    public ResponseEntity<Map<String, Object>> getFollowStats(@PathVariable Integer uid) {
        Map<String, Object> response = new HashMap<>();

        try {
            Integer fansCount = userFollowService.getFansCount(uid);
            Integer followingCount = userFollowService.getFollowingCount(uid);

            response.put("code", 200);
            response.put("message", "查询成功");
            response.put("data", Map.of(
                    "fansCount", fansCount,
                    "followingCount", followingCount
            ));
        } catch (Exception e) {
            log.error("获取关注统计失败", e);
            response.put("code", 500);
            response.put("message", "查询失败");
        }

        return ResponseEntity.ok(response);
    }
}
