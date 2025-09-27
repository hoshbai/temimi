package com.temimi.config;

import com.temimi.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
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
            "/api/video/list",
            "/api/video/detail/**",
            "/api/video/category",
            "/api/search",
            "/api/danmu/history",
            "/error",
            "/favicon.ico"
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
                Integer uid = jwtUtil.getUserIdFromToken(token);
                request.setAttribute("uid", uid);
                logger.debug("JWT验证成功，用户ID: " + uid);
            } catch (Exception e) {
                logger.warn("JWT Token验证失败: " + e.getMessage());
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

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.trim().isEmpty()) {
            return tokenParam;
        }

        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("{\"code\":%d,\"message\":\"%s\"}", status, message));
        response.getWriter().flush();
    }
}