package com.temimi.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和解析JWT令牌
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private Long expire;

    /**
     * 生成JWT Token
     * @param uid 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return JWT Token字符串
     */
    public String generateToken(Integer uid, String username, Integer role) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("uid", uid);
            claims.put("username", username);
            claims.put("role", role);
            claims.put("type", "access_token");

            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expire);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(username)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .setIssuer("shiyou-backend")
                    .signWith(getSignKey(), SignatureAlgorithm.HS512)
                    .compact();

        } catch (Exception e) {
            logger.error("生成JWT Token失败", e);
            throw new RuntimeException("生成Token失败", e);
        }
    }

    /**
     * 解析Token，获取Claims
     * @param token JWT Token
     * @return Claims对象
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT Token已过期: {}", e.getMessage());
            throw new RuntimeException("Token已过期", e);
        } catch (UnsupportedJwtException e) {
            logger.warn("不支持的JWT Token: {}", e.getMessage());
            throw new RuntimeException("不支持的Token格式", e);
        } catch (MalformedJwtException e) {
            logger.warn("JWT Token格式错误: {}", e.getMessage());
            throw new RuntimeException("Token格式错误", e);
        } catch (SignatureException e) {
            logger.warn("JWT Token签名验证失败: {}", e.getMessage());
            throw new RuntimeException("Token签名验证失败", e);
        } catch (IllegalArgumentException e) {
            logger.warn("JWT Token参数异常: {}", e.getMessage());
            throw new RuntimeException("Token参数异常", e);
        }
    }

    /**
     * 从Token中获取用户ID
     * @param token JWT Token
     * @return 用户ID
     */
    public Integer getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object uid = claims.get("uid");

        if (uid == null) {
            throw new RuntimeException("Token中未包含用户ID");
        }

        return Integer.valueOf(uid.toString());
    }

    /**
     * 从Token中获取用户名
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从Token中获取用户角色
     * @param token JWT Token
     * @return 用户角色
     */
    public Integer getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        Object role = claims.get("role");
        return role != null ? Integer.valueOf(role.toString()) : 0;
    }

    /**
     * 验证Token是否有效
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            logger.debug("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查Token是否即将过期（剩余时间少于30分钟）
     * @param token JWT Token
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();

            // 如果剩余时间少于30分钟，返回true
            long remainingTime = expiration.getTime() - now.getTime();
            return remainingTime < 30 * 60 * 1000; // 30分钟

        } catch (Exception e) {
            return true; // 解析失败，认为已过期
        }
    }

    /**
     * 刷新Token（生成新的Token，延长有效期）
     * @param token 原Token
     * @return 新Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            Integer uid = Integer.valueOf(claims.get("uid").toString());
            String username = claims.getSubject();
            Integer role = Integer.valueOf(claims.get("role").toString());

            return generateToken(uid, username, role);

        } catch (Exception e) {
            logger.error("刷新Token失败", e);
            throw new RuntimeException("刷新Token失败", e);
        }
    }

    /**
     * 获取签名密钥
     * @return SecretKey
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        // ✅ 安全增强：验证密钥长度（HS512 要求至少 64 字节）
        if (keyBytes.length < 64) {
            logger.error("JWT密钥长度不足！当前: {} 字节, 要求: 至少 64 字节 (HS512)", keyBytes.length);
            throw new IllegalArgumentException(
                String.format("JWT密钥长度必须至少 64 字节（HS512 要求），当前仅 %d 字节。" +
                    "请在 application.yml 中设置更长的 jwt.secret", keyBytes.length)
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}