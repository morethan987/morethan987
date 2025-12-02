package com.example.GradeSystemBackend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

    // 1. 定义密钥（生产环境应该写在配置文件里，这里为了演示直接写死）
    // 注意：密钥必须足够长（至少32个字符），否则报错
    private static final String SECRET_KEY =
        "MySuperSecretKeyForGradeSystemBackend2025!!!";
    private static final long EXPIRATION_TIME = 86400000; // 24小时

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    // 生成 Token
    public String generateToken(String username) {
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(getSigningKey())
            .compact();
    }

    // 从 Token 获取用户名
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    // 验证 Token 是否有效
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
