package com.temimi.controller.user;

import com.temimi.model.entity.User;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.UserService;
import com.temimi.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户账户管理控制器
 * 处理用户登录、注册等基础功能
 */
@RestController
@RequestMapping("/api/user/account")
@Validated
public class UserAccountController {

    private static final Logger logger = LoggerFactory.getLogger(UserAccountController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.temimi.service.UserDailyLoginService userDailyLoginService;

    /**
     * 用户登录
     * POST /api/user/account/login
     */
    @PostMapping("/login")
    public ApiResult<Map<String, Object>> login(@RequestBody @Valid UserLoginRequest request) {

        logger.info("用户尝试登录, username: {}", request.getUsername());

        try {
            // 调用service进行登录验证
            User user = userService.login(request.getUsername(), request.getPassword());

            // 处理每日登录奖励
            boolean gotReward = userDailyLoginService.processDailyLogin(user.getUid());

            // 如果获得了奖励，重新查询用户信息以获取最新的硬币数量
            if (gotReward) {
                user = userService.getById(user.getUid());
            }

            // 生成JWT Token
            String token = jwtUtil.generateToken(user.getUid(), user.getUsername(), user.getRole());

            // 清除敏感信息
            user.setPassword(null);

            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("user", user);
            result.put("token", token);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", 86400); // 24小时，单位：秒
            result.put("dailyReward", gotReward); // 是否获得了今日奖励

            logger.info("用户登录成功, uid: {}, username: {}, 今日奖励: {}", 
                user.getUid(), user.getUsername(), gotReward);
            return ApiResult.success(result);

        } catch (Exception e) {
            logger.error("用户登录失败, username: {}, error: {}", request.getUsername(), e.getMessage());
            return ApiResult.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册
     * POST /api/user/register
     */
    @PostMapping("/register")
    public ApiResult<User> register(@RequestBody @Valid UserRegisterRequest request) {

        logger.info("用户尝试注册, username: {}", request.getUsername());

        try {
            // 创建用户对象
            User user = new User();
            user.setUsername(request.getUsername().trim());
            user.setPassword(request.getPassword());
            // ✅ 如果用户提供了nickname，使用用户提供的；否则由service层自动生成
            if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
                user.setNickname(request.getNickname().trim());
            }
            user.setGender(request.getGender() != null ? request.getGender() : 2); // 默认未知
            user.setDescription(request.getDescription());

            // 调用service进行注册
            boolean success = userService.register(user);

            if (success) {
                // 查询刚注册的用户信息
                User registeredUser = userService.getUserByUsername(request.getUsername().trim());

                // 清除敏感信息
                if (registeredUser != null) {
                    registeredUser.setPassword(null);
                }

                logger.info("用户注册成功, username: {}", request.getUsername());
                return ApiResult.success(registeredUser);
            } else {
                logger.warn("用户注册失败, username: {}", request.getUsername());
                return ApiResult.error("注册失败");
            }

        } catch (Exception e) {
            logger.error("用户注册异常, username: {}, error: {}", request.getUsername(), e.getMessage());
            return ApiResult.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户退出登录
     * POST /api/user/logout
     *
     * ⚠️ 注意：JWT是无状态的，实际的token失效由前端删除token实现
     * 后端只做日志记录，如需实现token黑名单可在此扩展
     */
    @PostMapping("/logout")
    public ApiResult<String> logout() {
        try {
            // 从SecurityContext获取当前用户信息
            Integer uid = com.temimi.util.SecurityUtil.getCurrentUserId();
            if (uid != null) {
                logger.info("用户退出登录, uid: {}", uid);
            }

            // JWT无状态，无需后端处理token失效
            // 前端删除本地token即可
            return ApiResult.success("退出登录成功");

        } catch (Exception e) {
            logger.error("退出登录异常: {}", e.getMessage());
            // 即使失败也返回成功，让前端清除token
            return ApiResult.success("退出登录成功");
        }
    }

    /**
     * 检查用户名是否可用
     * GET /api/user/check-username?username=xxx
     */
    @GetMapping("/check-username")
    public ApiResult<Boolean> checkUsername(@RequestParam @NotBlank String username) {
        try {
            boolean available = !userService.existsByUsername(username.trim());
            return ApiResult.success(available);
        } catch (Exception e) {
            logger.error("检查用户名异常: {}", e.getMessage());
            return ApiResult.error("检查用户名失败");
        }
    }

    /**
     * 检查昵称是否可用
     * GET /api/user/check-nickname?nickname=xxx
     * ⚠️ 注意：由于nickname有UNIQUE约束，但注册时会自动生成，此接口用于测试兼容性
     */
    @GetMapping("/check-nickname")
    public ApiResult<Boolean> checkNickname(@RequestParam @NotBlank String nickname) {
        try {
            boolean available = !userService.existsByNickname(nickname.trim());
            return ApiResult.success(available);
        } catch (Exception e) {
            logger.error("检查昵称异常: {}", e.getMessage());
            return ApiResult.error("检查昵称失败");
        }
    }

    /**
     * 用户登录请求对象
     */
    public static class UserLoginRequest {

        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * 用户注册请求对象
     */
    public static class UserRegisterRequest {

        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度必须在3-20位之间")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 50, message = "密码长度必须在6-50位之间")
        private String password;

        // nickname字段改为可选，注册时自动生成
        @Size(max = 20, message = "昵称长度不能超过20个字符")
        private String nickname;

        private Integer gender; // 0女 1男 2未知

        @Size(max = 200, message = "个性签名不能超过200个字符")
        private String description;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }

        public Integer getGender() { return gender; }
        public void setGender(Integer gender) { this.gender = gender; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}