package com.bodimtikka.security;

import com.bodimtikka.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private Key key;
    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    /**
     * Generate JWT token with user ID as subject and optional roles
     */
    public String generateToken(Long userId, List<String> roles) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
                .signWith(key)
                .compact();
    }

    /**
     * Extract user ID from JWT token
     */
    public Long extractUserId(String token) {
        try {
            String sub = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            return Long.parseLong(sub);
        } catch (ExpiredJwtException e) {
            throw new JwtValidationException("JWT token has expired", e);
        } catch (MalformedJwtException e) {
            throw new JwtValidationException("JWT token is malformed", e);
        } catch (SecurityException  e) {
            throw new JwtValidationException("JWT signature is invalid", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtValidationException("JWT token is unsupported", e);
        } catch (IllegalArgumentException e) {
            throw new JwtValidationException("JWT token is empty or null", e);
        } catch (JwtException e) {
            throw new JwtValidationException("JWT token is invalid", e);
        }
    }

    /**
     * Extract roles from JWT token
     */
    public List<String> extractRoles(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Object rolesObj = claims.get("roles");
            if (rolesObj instanceof List<?> list) {
                return list.stream()
                        .map(Object::toString)
                        .toList(); // safely convert to List<String>
            }
            return Collections.emptyList();
        } catch (JwtException e) {
            throw new JwtValidationException("Cannot extract roles from JWT", e);
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return false;
        } catch (io.jsonwebtoken.JwtException e) {
            // malformed, unsupported, signature failed, ...
            return false;
        }
    }
}
