package com.community.communitycp.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：生成 / 解析 / 校验 Token
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * 签名密钥
     */
    @Value("${community.jwt.secret}")
    private String secret;

    /**
     * Token 有效期（毫秒）
     */
    @Value("${community.jwt.expire}")
    private Long expire;

    /**
     * Token 前缀
     */
    @Value("${community.jwt.prefix}")
    private String prefix;

    /**
     * 生成签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Token
     *
     * @param userId 用户ID
     * @param role   用户角色
     * @return token 字符串（不含前缀）
     */
    public String generateToken(Long userId, Integer role) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expire);

        return Jwts.builder()
                .subject(String.valueOf(userId))       // 主题：存用户ID
                .claim("role", role)                   // 自定义声明：存用户角色
                .issuedAt(now)                          // 签发时间
                .expiration(expireDate)                 // 过期时间
                .signWith(getSigningKey())              // 签名
                .compact();
    }

    /**
     * 从 Token 中解析 Claims（校验签名 + 过期时间）
     *
     * @param token token 字符串（不含前缀）
     * @return Claims，解析失败返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.warn("Token 解析失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取用户ID
     *
     * @param token token 字符串（不含前缀）
     * @return 用户ID，解析失败返回 null
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        String subject = claims.getSubject();
        if (subject == null) {
            return null;
        }
        return Long.valueOf(subject);
    }

    /**
     * 从 Token 中获取用户角色
     *
     * @param token token 字符串（不含前缀）
     * @return 用户角色，解析失败返回 null
     */
    public Integer getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object role = claims.get("role");
        if (role == null) {
            return null;
        }
        return ((Number) role).intValue();
    }

    /**
     * 校验 Token 是否有效
     *
     * @param token token 字符串（不含前缀）
     * @return true=有效
     */
    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }

    /**
     * 从完整的 Authorization 请求头中提取纯 token
     * 例："Bearer eyJhbGci..." → "eyJhbGci..."
     *
     * @param header Authorization 请求头的值
     * @return 纯 token，格式不对返回 null
     */
    public String resolveToken(String header) {
        if (header == null || !header.startsWith(prefix)) {
            return null;
        }
        return header.substring(prefix.length());
    }
}
