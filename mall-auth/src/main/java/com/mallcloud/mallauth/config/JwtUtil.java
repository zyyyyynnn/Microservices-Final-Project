package com.mallcloud.mallauth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import com.mallcloud.mallcommon.constant.CommonConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT 工具类
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 64) {
            // Need a strong enough key for HS512.
            StringBuilder sb = new StringBuilder(secret);
            while(sb.length() < 64) {
                sb.append(secret);
            }
            keyBytes = sb.toString().substring(0, 64).getBytes();
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Long userId, List<String> roles) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .claim("uid", userId)
                .claim("roles", roles)
                .claim(CommonConstants.JWT_CLAIM_TOKEN_TYPE, CommonConstants.JWT_TOKEN_TYPE_ACCESS)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .claim("uid", userId)
                .claim(CommonConstants.JWT_CLAIM_TOKEN_TYPE, CommonConstants.JWT_TOKEN_TYPE_REFRESH)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
