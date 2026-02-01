package com.temimi.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 * 用于从 Spring Security 上下文获取当前登录用户信息
 *
 * ✅ 修复：启用 JWT 后，Controller 不再直接从 @RequestHeader("uid") 获取用户ID
 * ✅ 而是通过 SecurityContext 获取，防止伪造 uid
 */
public class SecurityUtil {

    /**
     * 获取当前登录用户的 UID
     *
     * @return 用户ID，如果未登录返回 null
     */
    public static Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // JwtAuthenticationTokenFilter 中设置的 principal 是 uid
        Object principal = authentication.getPrincipal();

        if (principal instanceof Integer) {
            return (Integer) principal;
        }

        return null;
    }

    /**
     * 获取当前登录用户的 UID（必须登录）
     *
     * @return 用户ID
     * @throws SecurityException 如果未登录
     */
    public static Integer getCurrentUserIdRequired() {
        Integer uid = getCurrentUserId();

        if (uid == null) {
            throw new SecurityException("用户未登录或登录已过期");
        }

        return uid;
    }

    /**
     * 检查当前用户是否有权限操作指定资源
     *
     * @param resourceOwnerId 资源所有者的 UID
     * @return true 如果当前用户是资源所有者
     */
    public static boolean hasPermission(Integer resourceOwnerId) {
        Integer currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(resourceOwnerId);
    }

    /**
     * 检查当前用户是否是管理员
     *
     * @return true 如果是管理员或超级管理员
     */
    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // 检查是否有管理员或超级管理员角色
        return authentication.getAuthorities().stream()
            .anyMatch(authority ->
                authority.getAuthority().equals("ROLE_ADMIN") ||
                authority.getAuthority().equals("ROLE_SUPER_ADMIN")
            );
    }

    /**
     * 检查当前用户是否是超级管理员
     *
     * @return true 如果是超级管理员
     */
    public static boolean isSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    /**
     * 要求当前用户是管理员，否则抛出异常
     *
     * @throws SecurityException 如果不是管理员
     */
    public static void requireAdmin() {
        if (!isAdmin()) {
            throw new SecurityException("需要管理员权限才能执行此操作");
        }
    }

    /**
     * 要求当前用户是超级管理员，否则抛出异常
     *
     * @throws SecurityException 如果不是超级管理员
     */
    public static void requireSuperAdmin() {
        if (!isSuperAdmin()) {
            throw new SecurityException("需要超级管理员权限才能执行此操作");
        }
    }

    /**
     * 检查当前用户是否有权限操作指定资源（所有者或管理员）
     *
     * @param resourceOwnerId 资源所有者的 UID
     * @return true 如果当前用户是资源所有者或管理员
     */
    public static boolean hasPermissionOrAdmin(Integer resourceOwnerId) {
        return isAdmin() || hasPermission(resourceOwnerId);
    }

    /**
     * 清除 SecurityContext（用于登出）
     */
    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
