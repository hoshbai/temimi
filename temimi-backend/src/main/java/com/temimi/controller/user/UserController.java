package com.temimi.controller.user;

import com.temimi.model.entity.User;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息控制器
 * 处理用户登录后的个人信息管理，如查看和修改资料。
 * 注意：所有接口都需要用户已登录，通过 JWT Token 获取用户ID。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 根据用户ID获取用户信息 (用于个人空间)
     * GET /api/user/profile/{uid}
     *
     * @param uid 用户ID
     * @return 包含用户信息的ApiResult
     */
    @GetMapping("/profile/{uid}")
    public ApiResult<User> getUserProfile(@PathVariable Integer uid) {
        try {
            // 1. 调用Service层获取用户信息
            User user = userService.getUserById(uid);
            if (user == null) {
                return ApiResult.error("用户不存在");
            }

            // 2. 安全处理：清除敏感信息（如密码），避免泄露
            user.setPassword(null);

            // 3. 返回成功响应
            return ApiResult.success(user);
        } catch (Exception e) {
            // 4. 捕获异常，返回错误信息
            return ApiResult.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户个人信息 (昵称、头像、简介等)
     * PUT /api/user/profile
     *
     * @param user 要更新的用户对象 (前端传递)
     * @param currentUid 当前登录用户的ID (从JWT Token中解析，通过过滤器放入Request属性)
     * @return 操作结果
     */
    @PutMapping("/profile")
    public ApiResult<String> updateUserProfile(@RequestBody User user, @RequestHeader("uid") Integer currentUid) {
        try {
            // 1. 安全校验：确保用户只能修改自己的资料
            if (!currentUid.equals(user.getUid())) {
                return ApiResult.error("无权修改他人资料");
            }

            // 2. 调用Service层更新用户信息
            // 注意：Service层应只更新允许用户修改的字段（nickname, avatar, description, gender）
            boolean success = userService.updateUserInfo(user);

            // 3. 根据更新结果返回响应
            if (success) {
                return ApiResult.success("用户信息更新成功");
            } else {
                return ApiResult.error("用户信息更新失败");
            }
        } catch (Exception e) {
            return ApiResult.error("更新用户信息失败: " + e.getMessage());
        }
    }
}