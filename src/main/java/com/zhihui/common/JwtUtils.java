package com.zhihui.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 工具类
 * 用于生成、解析和验证 JWT 令牌
 */
@Component
public class JwtUtils {
    /**
     * JWT 密钥，从配置文件中读取
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT 令牌过期时间（毫秒），从配置文件中读取
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 获取签名密钥
     * 将 Base64 编码的密钥解码并转换为 HMAC-SHA 密钥
     *
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT 令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return 生成的 JWT 令牌字符串
     */
    public String generateToken(Long userId, String username) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从 JWT 令牌中获取用户ID
     *
     * @param token JWT 令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 验证 JWT 令牌是否有效
     *
     * @param token JWT 令牌
     * @return 如果令牌有效返回 true，否则返回 false
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 解析 JWT 令牌
     *
     * @param token JWT 令牌
     * @return 令牌中的声明信息
     * @throws JwtException 如果令牌无效或已过期
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}