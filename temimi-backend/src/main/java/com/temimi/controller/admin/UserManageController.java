package com.temimi.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.temimi.model.entity.User;
import com.temimi.model.vo.ApiResult;
import com.temimi.service.UserService;
import com.temimi.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制器（管理后台）
 */
@RestController
@RequestMapping({"/api/admin/user", "/admin/user"})
public class UserManageController {

    private static final Logger logger = LoggerFactory.getLogger(UserManageController.class);

    @Autowired
    private UserService userService;

    /**
     * 分页获取用户列表
     */
    @GetMapping("/list")
    public ApiResult<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer state,
            @RequestParam(required = false) Integer role) {
        try {
            SecurityUtil.requireAdmin();
            
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            
            // 关键词搜索（用户名或昵称）
            if (keyword != null && !keyword.trim().isEmpty()) {
                queryWrapper.and(w -> w
                    .like("username", keyword)
                    .or()
                    .like("nickname", keyword)
                    .or()
                    .eq("uid", keyword.matches("\\d+") ? Integer.parseInt(keyword) : -1)
                );
            }
            
            // 状态筛选
            if (state != null) {
                queryWrapper.eq("state", state);
            }
            
            // 角色筛选
            if (role != null) {
                queryWrapper.eq("role", role);
            }
            
            queryWrapper.orderByDesc("create_date");
            
            Page<User> userPage = new Page<>(page, size);
            Page<User> result = userService.page(userPage, queryWrapper);
            
            // 隐藏密码
            result.getRecords().forEach(u -> u.setPassword(null));
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", result.getRecords());
            data.put("total", result.getTotal());
            data.put("pages", result.getPages());
            data.put("current", result.getCurrent());
            
            return ApiResult.success(data);
        } catch (SecurityException e) {
            return ApiResult.error(403, e.getMessage());
        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 获取单个用户详情
     */
    @GetMapping("/detail/{uid}")
    public ApiResult<User> getUserDetail(@PathVariable Integer uid) {
        try {
            SecurityUtil.requireAdmin();
            
            User user = userService.getById(uid);
            if (user == null) {
                return ApiResult.error("用户不存在");
            }
            user.setPassword(null);
            return ApiResult.success(user);
        } catch (SecurityException e) {
            return ApiResult.error(403, e.getMessage());
        } catch (Exception e) {
            logger.error("获取用户详情失败", e);
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 更新用户状态（封禁/解封）
     */
    @PostMapping("/state")
    public ApiResult<Void> updateUserState(@RequestBody Map<String, Object> params) {
        try {
            SecurityUtil.requireAdmin();
            
            Integer uid = (Integer) params.get("uid");
            Integer state = (Integer) params.get("state");
            
            if (uid == null || state == null) {
                return ApiResult.error("参数不完整");
            }
            
            // 不能操作自己
            Integer currentUid = SecurityUtil.getCurrentUserId();
            if (uid.equals(currentUid)) {
                return ApiResult.error("不能修改自己的状态");
            }
            
            User user = userService.getById(uid);
            if (user == null) {
                return ApiResult.error("用户不存在");
            }
            
            // 普通管理员不能操作管理员
            if (!SecurityUtil.isSuperAdmin() && user.getRole() != null && user.getRole() > 0) {
                return ApiResult.error("权限不足，无法操作管理员账号");
            }
            
            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("uid", uid).set("state", state);
            userService.update(updateWrapper);
            
            return ApiResult.success(null);
        } catch (SecurityException e) {
            return ApiResult.error(403, e.getMessage());
        } catch (Exception e) {
            logger.error("更新用户状态失败", e);
            return ApiResult.error(e.getMessage());
        }
    }

    /**
     * 更新用户角色（超级管理员专属）
     */
    @PostMapping("/role")
    public ApiResult<Void> updateUserRole(@RequestBody Map<String, Object> params) {
        try {
            SecurityUtil.requireSuperAdmin();
            
            Integer uid = (Integer) params.get("uid");
            Integer role = (Integer) params.get("role");
            
            if (uid == null || role == null) {
                return ApiResult.error("参数不完整");
            }
            
            // 不能操作自己
            Integer currentUid = SecurityUtil.getCurrentUserId();
            if (uid.equals(currentUid)) {
                return ApiResult.error("不能修改自己的角色");
            }
            
            User user = userService.getById(uid);
            if (user == null) {
                return ApiResult.error("用户不存在");
            }
            
            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("uid", uid).set("role", role);
            userService.update(updateWrapper);
            
            return ApiResult.success(null);
        } catch (SecurityException e) {
            return ApiResult.error(403, e.getMessage());
        } catch (Exception e) {
            logger.error("更新用户角色失败", e);
            return ApiResult.error(e.getMessage());
        }
    }
}
