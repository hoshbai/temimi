package com.temimi.controller.admin;

import com.temimi.model.entity.User;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.UserService;
import com.temimi.util.JwtUtil;
import com.temimi.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员账户控制器
 * 处理管理员登录、登出等功能
 */
@RestController
@RequestMapping({"/api/admin/account", "/admin/account"})
@Validated
public class AdminAccountController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAccountController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 管理员登录
     * POST /api/admin/account/login
     */
    @PostMapping("/login")
    public ApiResult<Map<String, Object>> login(@RequestBody @Valid AdminLoginRequest request) {
        logger.info("管理员尝试登录, username: {}", request.getUsername());

        try {
            // 调用service进行登录验证
            User user = userService.login(request.getUsername(), request.getPassword());

            // 检查是否是管理员 (role >= 1)
            if (user.getRole() == null || user.getRole() < 1) {
                logger.warn("非管理员尝试登录管理后台, username: {}, role: {}", 
                    request.getUsername(), user.getRole());
                return ApiResult.error("您没有管理员权限");
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
            result.put("expiresIn", 86400); // 24小时

            logger.info("管理员登录成功, uid: {}, username: {}, role: {}", 
                user.getUid(), user.getUsername(), user.getRole());
            return ApiResult.success(result);

        } catch (Exception e) {
            logger.error("管理员登录失败, username: {}, error: {}", request.getUsername(), e.getMessage());
            return ApiResult.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 管理员登出
     * GET /api/admin/account/logout
     */
    @GetMapping("/logout")
    public ApiResult<String> logout() {
        try {
            Integer uid = SecurityUtil.getCurrentUserId();
            if (uid != null) {
                logger.info("管理员退出登录, uid: {}", uid);
            }
            return ApiResult.success("退出登录成功");
        } catch (Exception e) {
            return ApiResult.success("退出登录成功");
        }
    }

    /**
     * 获取当前管理员信息
     * GET /api/admin/personal/info
     */
    @GetMapping("/info")
    public ApiResult<User> getAdminInfo() {
        try {
            Integer uid = SecurityUtil.getCurrentUserIdRequired();
            User user = userService.getById(uid);
            
            if (user == null) {
                return ApiResult.error("用户不存在");
            }
            
            // 检查是否是管理员
            if (user.getRole() == null || user.getRole() < 1) {
                return ApiResult.error("您没有管理员权限");
            }
            
            user.setPassword(null);
            return ApiResult.success(user);
        } catch (Exception e) {
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 管理员登录请求对象
     */
    public static class AdminLoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
