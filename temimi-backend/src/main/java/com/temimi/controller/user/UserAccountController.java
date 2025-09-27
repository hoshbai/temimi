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
import java.util.HashMap;
import java.util.Map;

/**
 * 用户账户管理控制器
 * 处理用户登录、注册等基础功能
 */
@RestController
@RequestMapping("/api/user")
@Validated
public class UserAccountController {

    private static final Logger logger = LoggerFactory.getLogger(UserAccountController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     * POST /api/user/login
     */
    @PostMapping("/login")
    public ApiResult<Map<String, Object>> login(
            @RequestParam @NotBlank(message = "用户名不能为空") String username,
            @RequestParam @NotBlank(message = "密码不能为空") String password) {

        logger.info("用户尝试登录, username: {}", username);

        try {
            // 调用service进行登录验证
            User user = userService.login(username, password);

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

            logger.info("用户登录成功, uid: {}, username: {}", user.getUid(), user.getUsername());
            return ApiResult.success(result);

        } catch (Exception e) {
            logger.error("用户登录失败, username: {}, error: {}", username, e.getMessage());
            return ApiResult.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册
     * POST /api/user/register
     */
    @PostMapping("/register")
    public ApiResult<String> register(@RequestBody @Valid UserRegisterRequest request) {

        logger.info("用户尝试注册, username: {}, nickname: {}", request.getUsername(), request.getNickname());

        try {
            // 创建用户对象
            User user = new User();
            user.setUsername(request.getUsername().trim());
            user.setPassword(request.getPassword());
            user.setNickname(request.getNickname().trim());
            user.setGender(request.getGender() != null ? request.getGender() : 2); // 默认未知
            user.setDescription(request.getDescription());

            // 调用service进行注册
            boolean success = userService.register(user);

            if (success) {
                logger.info("用户注册成功, username: {}", request.getUsername());
                return ApiResult.success("注册成功");
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
     * 检查用户名是否可用
     * GET /api/user/check-username?username=xxx
     */
    @GetMapping("/check-username")
    public ApiResult<Boolean> checkUsername(@RequestParam @NotBlank String username) {
        try {
            User existingUser = userService.getUserByUsername(username.trim());
            boolean available = (existingUser == null);
            return ApiResult.success(available);
        } catch (Exception e) {
            logger.error("检查用户名异常: {}", e.getMessage());
            return ApiResult.error("检查用户名失败");
        }
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

        @NotBlank(message = "昵称不能为空")
        @Size(min = 1, max = 20, message = "昵称长度必须在1-20位之间")
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