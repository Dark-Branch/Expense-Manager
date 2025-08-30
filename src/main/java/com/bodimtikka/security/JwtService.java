package com.bodimtikka.security;

import com.bodimtikka.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

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
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            throw new JwtValidationException("JWT token has expired", e);
        } catch (MalformedJwtException e) {
            throw new JwtValidationException("JWT token is malformed", e);
        } catch (SecurityException e) {
            throw new JwtValidationException("JWT signature is invalid", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtValidationException("JWT token is unsupported", e);
        } catch (IllegalArgumentException e) {
            throw new JwtValidationException("JWT token is empty or null", e);
        } catch (JwtException e) {
            throw new JwtValidationException("JWT token is invalid", e);
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtValidationException e) {
            return false;
        }
    }
}
