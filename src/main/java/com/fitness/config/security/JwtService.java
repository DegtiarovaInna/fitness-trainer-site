package com.fitness.config.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;


import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refreshExpiration}")
    private long refreshExpiration;
    @Value("${jwt.resetExpiration}")
    private long resetExpiration;

    public String generateToken(String email) {
        return buildToken(email, expiration, "access");
    }

    public String generateRefreshToken(String email) {
        return buildToken(email, refreshExpiration, "refresh");
    }

    private String buildToken(String email, long ttlMillis, String type) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlMillis);
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("type", type)
                .signWith(getSignKey())
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims c = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return c.getSubject().equals(userDetails.getUsername())
                    && c.getExpiration().after(new Date())
                    && "access".equals(c.get("type", String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            String type = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("type", String.class);
            return "refresh".equals(type);
        } catch (JwtException e) {
            return false;
        }
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    public String generateResetToken(String email) {
        return buildToken(email, resetExpiration, "reset");
    }

    public boolean isResetToken(String token, UserDetails userDetails) {
        try {
            Claims c = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return c.getSubject().equals(userDetails.getUsername())
                    && c.getExpiration().after(new Date())
                    && "reset".equals(c.get("type", String.class));
        } catch (JwtException e) {
            return false;
        }
    }
}
