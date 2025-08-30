package com.bodimtikka.dto.auth;

public record AuthResponse(Long userId, String name, String email, String token) {
}
