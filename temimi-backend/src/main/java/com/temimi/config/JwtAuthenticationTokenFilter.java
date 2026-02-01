package com.temimi.config;

import com.temimi.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Order(1) // 设置过滤器执行顺序
@ConditionalOnProperty(name = "jwt.enabled", havingValue = "true", matchIfMissing = false)
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/test/**",
            "/api/user/login",
            "/api/user/register",
            "/api/user/account/login",      // ✅ 实际的登录路径
            "/api/user/account/register",   // ✅ 实际的注册路径
            "/api/admin/account/login",     // ✅ 管理员登录路径
            "/admin/account/login",         // ✅ 管理员登录路径（代理后）
            "/api/user/check-username",
            "/api/user/check-nickname",
            "/api/user/profile/**",         // ✅ 允许匿名访问用户资料
            "/api/video/list",
            "/api/video/detail/**",
            "/api/video/category",
            "/api/video/user/**",           // ✅ 允许匿名访问用户视频
            "/api/video/user-works-count",  // ✅ 允许匿名访问用户投稿数
            "/api/video/user-works",        // ✅ 允许匿名访问用户投稿列表
            "/api/video/user-love",         // ✅ 允许匿名访问用户点赞视频
            "/api/category/**",
            "/category/**",                 // ✅ 分类接口（代理后）
            "/api/comment/get",             // ✅ 只允许匿名访问获取评论列表
            "/api/comment/get-up-like",     // ✅ 只允许匿名访问UP主点赞的评论
            "/api/comment/root",            // ✅ 只允许匿名访问根评论
            "/api/comment/tree/**",         // ✅ 只允许匿名访问评论树
            "/api/danmu/get",               // ✅ 只允许匿名访问获取弹幕
            "/api/favorite/user/**",        // ✅ 允许匿名访问用户收藏夹
            "/api/search/**",   // ✅ 允许匿名访问搜索功能
            "/api/ws/**",       // ✅ WebSocket 相关接口
            "/ws/**",           // ✅ WebSocket 端点（包括 /ws/danmu/**）
            "/im",              // ✅ WebSocket 即时通讯端点
            "/chat",            // ✅ WebSocket 聊天端点
            "/danmu",           // ✅ WebSocket 弹幕端点
            "/error",
            "/favicon.ico",
            // ✅ 静态资源路径 - 允许匿名访问
            "/avatars/**",
            "/covers/**",
            "/videos/**",
            "/backgrounds/**"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        logger.debug("JWT过滤器检查路径: " + path);

        boolean shouldExclude = EXCLUDE_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        logger.debug("是否跳过JWT验证: " + shouldExclude);
        return shouldExclude;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        logger.debug("JWT过滤器执行，请求路径: " + request.getRequestURI());

        String token = getTokenFromRequest(request);

        if (token != null) {
            try {
                // 解析 Token 获取用户信息
                Integer uid = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                Integer role = jwtUtil.getRoleFromToken(token);

                // ========== 改进：使用 Spring Security 标准方式 ==========
                // 创建 Authentication 对象
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + getRoleName(role))
                );

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(uid, null, authorities);

                // 设置到 SecurityContext（标准做法）
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 同时保留 request attribute 以兼容现有代码
                request.setAttribute("uid", uid);
                request.setAttribute("username", username);
                request.setAttribute("role", role);

                logger.debug("JWT验证成功，用户ID: " + uid + ", 用户名: " + username + ", 角色: " + role);

            } catch (Exception e) {
                logger.warn("JWT Token验证失败: " + e.getMessage());
                SecurityContextHolder.clearContext(); // 清除 SecurityContext
                sendErrorResponse(response, 401, "Token无效或已过期");
                return;
            }
        } else {
            logger.warn("未提供JWT Token，路径: " + request.getRequestURI());
            sendErrorResponse(response, 401, "未提供认证令牌");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * 从 Token 中获取
     * 注意：不再支持 URL 参数传递 Token（安全考虑）
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 将角色 ID 转换为角色名称
     */
    private String getRoleName(Integer role) {
        if (role == null) {
            return "USER";
        }
        switch (role) {
            case 1:
                return "ADMIN";
            case 2:
                return "SUPER_ADMIN";
            default:
                return "USER";
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("{\"code\":%d,\"message\":\"%s\"}", status, message));
        response.getWriter().flush();
    }
}