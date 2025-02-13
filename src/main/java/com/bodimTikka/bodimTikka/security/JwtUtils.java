package com.bodimTikka.bodimTikka.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SignatureException;
import java.util.Date;

@Component
public class JwtUtils {
    private final Key key = Keys.hmacShaKeyFor(
            "N5a7V8c9X2d4T6f1P3k6Y9wdvs2Z8q4R5m7J1x3L6p9S2d8B4N7C5G9Q2W8T4M1F3".getBytes(StandardCharsets.UTF_8)
    );

    private final long jwtExpirationMs = 86400000; // 1 day

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Date date = new Date();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(date)
                .setExpiration(new Date(date.getTime() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
//        } catch (SignatureException ex) {
//            throw new AuthenticationException("Invalid JWT signature") {};  // Ensures 401
        } catch (ExpiredJwtException ex) {
            throw new AuthenticationException("JWT token is expired") {};  // Ensures 401
        } catch (MalformedJwtException ex) {
            throw new AuthenticationException("Malformed JWT token") {};  // Ensures 401
        } catch (UnsupportedJwtException ex) {
            throw new AuthenticationException("Unsupported JWT token") {};  // Ensures 401
        } catch (IllegalArgumentException ex) {
            throw new AuthenticationException("JWT claims string is empty") {};  // Ensures 401
        }
    }
}
