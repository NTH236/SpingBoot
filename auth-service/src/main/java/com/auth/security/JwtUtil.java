package com.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

/**
 * Tiện ích xử lý JSON Web Token (JWT) trong auth-service.
 * Chịu trách nhiệm tạo token và xác thực chữ ký token.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:soa-architecture-jwt-secret-key-very-secure-2026-super-strong-token-signature}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expirationTime;

    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Tạo mới một JWT Token cho người dùng dựa vào username.
     *
     * @param userName Tên tài khoản
     * @return Chuỗi token đã được ký số
     */
    public String generateToken(String userName) {
        return Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Xác thực tính toàn vẹn và hạn sử dụng của JWT Token.
     *
     * @param token Chuỗi JWT token
     * @return true nếu hợp lệ; false nếu không hợp lệ hoặc đã hết hạn
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Trích xuất tên người dùng (Subject) từ JWT Token.
     *
     * @param token Chuỗi JWT token
     * @return Tên người dùng nếu token hợp lệ, null nếu không hợp lệ
     */
    public String extractUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
