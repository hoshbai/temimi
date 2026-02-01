package com.temimi.controller.user;

import com.temimi.model.entity.User;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.UserService;
import com.temimi.service.UserFollowService;
import com.temimi.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息控制器
 * 处理用户登录后的个人信息管理，如查看和修改资料。
 * 注意：所有接口都需要用户已登录，通过 JWT Token 获取用户ID。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserFollowService userFollowService;

    /**
     * 获取当前登录用户的个人信息
     * GET /api/user/personal/info
     *
     * @return 包含当前用户信息的ApiResult
     */
    @GetMapping("/personal/info")
    public ApiResult<User> getPersonalInfo() {
        try {
            // 从 SecurityContext 获取当前登录用户 ID
            Integer currentUid = SecurityUtil.getCurrentUserIdRequired();

            // 调用Service层获取用户信息
            User user = userService.getUserById(currentUid);
            if (user == null) {
                return ApiResult.error("用户不存在");
            }

            // 清除敏感信息
            user.setPassword(null);
            
            // 获取关注和粉丝数
            Integer followsCount = userFollowService.getFollowingCount(currentUid);
            Integer fansCount = userFollowService.getFansCount(currentUid);
            user.setFollowsCount(followsCount);
            user.setFansCount(fansCount);

            return ApiResult.success(user);
        } catch (Exception e) {
            logger.error("获取个人信息失败: {}", e.getMessage());
            return ApiResult.error("获取个人信息失败: " + e.getMessage());
        }
    }

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
            
            // 3. 获取关注和粉丝数
            Integer followsCount = userFollowService.getFollowingCount(uid);
            Integer fansCount = userFollowService.getFansCount(uid);
            user.setFollowsCount(followsCount);
            user.setFansCount(fansCount);

            // 4. 返回成功响应
            return ApiResult.success(user);
        } catch (Exception e) {
            // 5. 捕获异常，返回错误信息
            return ApiResult.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户个人信息 (昵称、头像、简介等)
     * PUT /api/user/profile
     *
     * @param user 要更新的用户对象 (前端传递)
     * @return 操作结果
     * ✅ 修复：从 SecurityContext 获取 UID，防止伪造
     */
    @PutMapping("/profile")
    public ApiResult<String> updateUserProfile(@RequestBody User user) {
        try {
            // 1. 从 SecurityContext 获取当前登录用户 ID
            Integer currentUid = SecurityUtil.getCurrentUserIdRequired();

            // 2. 安全校验：确保用户只能修改自己的资料
            if (!currentUid.equals(user.getUid())) {
                return ApiResult.error("无权修改他人资料");
            }

            // 3. 调用Service层更新用户信息
            // 注意：Service层应只更新允许用户修改的字段（nickname, avatar, description, gender）
            boolean success = userService.updateUserInfo(user);

            // 4. 根据更新结果返回响应
            if (success) {
                return ApiResult.success("用户信息更新成功");
            } else {
                return ApiResult.error("用户信息更新失败");
            }
        } catch (Exception e) {
            return ApiResult.error("更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户个人信息 (前端兼容接口 - FormData格式)
     * POST /api/user/info/update
     *
     * @param nickname 昵称
     * @param description 个人简介
     * @param gender 性别 (0=女, 1=男, 2=保密)
     * @return 操作结果
     * ✅ 修复：支持前端 FormData 格式，从 SecurityContext 获取 UID
     */
    @PostMapping("/info/update")
    public ApiResult<String> updateUserInfo(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer gender) {
        try {
            // 1. 从 SecurityContext 获取当前登录用户 ID
            Integer currentUid = SecurityUtil.getCurrentUserIdRequired();

            // 2. 构造 User 对象
            User user = new User();
            user.setUid(currentUid);
            if (nickname != null) {
                user.setNickname(nickname.trim());
            }
            if (description != null) {
                user.setDescription(description);
            }
            if (gender != null) {
                user.setGender(gender);
            }

            // 3. 调用Service层更新用户信息
            boolean success = userService.updateUserInfo(user);

            // 4. 根据更新结果返回响应
            if (success) {
                return ApiResult.success("用户信息更新成功");
            } else {
                return ApiResult.error("用户信息更新失败");
            }
        } catch (Exception e) {
            logger.error("更新用户信息失败: {}", e.getMessage());
            return ApiResult.error("更新用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 修改用户密码
     * PUT /api/user/password
     * ✅ 修复：从 SecurityContext 获取 UID，防止伪造
     */
    @PutMapping("/password")
    public ApiResult<String> updatePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            // 1. 从 SecurityContext 获取当前登录用户 ID
            Integer currentUid = SecurityUtil.getCurrentUserIdRequired();

            // 2. 验证旧密码
            boolean isValidOldPassword = userService.validatePassword(currentUid, oldPassword);
            if (!isValidOldPassword) {
                return ApiResult.error("旧密码错误");
            }

            // 3. 更新密码
            boolean success = userService.updatePassword(currentUid, newPassword);
            if (success) {
                return ApiResult.success("密码修改成功");
            } else {
                return ApiResult.error("密码修改失败");
            }

        } catch (Exception e) {
            return ApiResult.error("修改密码失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传用户头像
     * POST /api/user/avatar
     *
     * @param file 头像文件
     * @return 上传结果，包含新头像URL
     */
    @PostMapping("/avatar")
    public ApiResult<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 从 SecurityContext 获取当前登录用户 ID
            Integer currentUid = SecurityUtil.getCurrentUserIdRequired();

            logger.info("用户 {} 正在上传头像，文件名: {}, 大小: {} bytes",
                    currentUid, file.getOriginalFilename(), file.getSize());

            // 调用 Service 处理文件上传
            String avatarUrl = userService.uploadAvatar(file, currentUid);

            // 返回新头像URL
            Map<String, String> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);

            logger.info("用户 {} 头像上传成功，URL: {}", currentUid, avatarUrl);
            return ApiResult.success(result);

        } catch (IllegalArgumentException e) {
            logger.warn("头像上传失败 - 参数错误: {}", e.getMessage());
            return ApiResult.error(400, e.getMessage());
        } catch (Exception e) {
            logger.error("头像上传失败: {}", e.getMessage(), e);
            return ApiResult.error("头像上传失败，请稍后重试");
        }
    }

    /**
     * 更新用户头像（前端兼容接口）
     * POST /api/user/avatar/update
     *
     * @param file 头像文件
     * @return 上传结果，包含新头像URL
     */
    @PostMapping("/avatar/update")
    public ApiResult<Map<String, String>> updateAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 从 SecurityContext 获取当前登录用户 ID
            Integer currentUid = SecurityUtil.getCurrentUserIdRequired();

            logger.info("用户 {} 正在更新头像，文件名: {}, 大小: {} bytes",
                    currentUid, file.getOriginalFilename(), file.getSize());

            // 调用 Service 处理文件上传
            String avatarUrl = userService.uploadAvatar(file, currentUid);

            // 返回新头像URL
            Map<String, String> result = new HashMap<>();
            result.put("url", avatarUrl);  // 前端期望的字段名
            result.put("avatarUrl", avatarUrl);

            logger.info("用户 {} 头像更新成功，URL: {}", currentUid, avatarUrl);
            return ApiResult.success(result);

        } catch (IllegalArgumentException e) {
            logger.warn("头像更新失败 - 参数错误: {}", e.getMessage());
            return ApiResult.error(400, e.getMessage());
        } catch (Exception e) {
            logger.error("头像更新失败: {}", e.getMessage(), e);
            return ApiResult.error("头像更新失败，请稍后重试");
        }
    }

    /**
     * 上传用户空间背景图
     * POST /api/user/background/upload
     *
     * @param file 背景图文件
     * @return 上传结果，包含新背景图URL
     */
    @PostMapping("/background/upload")
    public ApiResult<String> uploadBackground(@RequestParam("file") MultipartFile file) {
        try {
            // 从 SecurityContext 获取当前登录用户 ID
            Integer currentUid = SecurityUtil.getCurrentUserIdRequired();

            logger.info("用户 {} 正在上传背景图，文件名: {}, 大小: {} bytes",
                    currentUid, file.getOriginalFilename(), file.getSize());

            // 调用 Service 处理文件上传
            String bgUrl = userService.uploadBackground(file, currentUid);

            logger.info("用户 {} 背景图上传成功，URL: {}", currentUid, bgUrl);
            return ApiResult.success(bgUrl);

        } catch (IllegalArgumentException e) {
            logger.warn("背景图上传失败 - 参数错误: {}", e.getMessage());
            return ApiResult.error(400, e.getMessage());
        } catch (Exception e) {
            logger.error("背景图上传失败: {}", e.getMessage(), e);
            return ApiResult.error("背景图上传失败，请稍后重试");
        }
    }

    /**
     * 更新用户空间背景图URL
     * POST /api/user/background/update
     *
     * @param bgUrl 背景图URL
     * @return 操作结果
     */
    @PostMapping("/background/update")
    public ApiResult<String> updateBackground(@RequestParam String bgUrl) {
        try {
            // 从 SecurityContext 获取当前登录用户 ID
            Integer currentUid = SecurityUtil.getCurrentUserIdRequired();

            logger.info("用户 {} 正在更新背景图，URL: {}", currentUid, bgUrl);

            // 调用 Service 更新背景图
            boolean success = userService.updateBackground(currentUid, bgUrl);

            if (success) {
                logger.info("用户 {} 背景图更新成功", currentUid);
                return ApiResult.success("背景图更新成功");
            } else {
                return ApiResult.error("背景图更新失败");
            }

        } catch (Exception e) {
            logger.error("背景图更新失败: {}", e.getMessage(), e);
            return ApiResult.error("背景图更新失败，请稍后重试");
        }
    }
}