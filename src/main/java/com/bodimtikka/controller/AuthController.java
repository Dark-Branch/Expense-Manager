package com.bodimtikka.controller;

import com.bodimtikka.dto.auth.AuthResponse;
import com.bodimtikka.dto.auth.LoginRequest;
import com.bodimtikka.dto.auth.RegisterRequest;
import com.bodimtikka.model.User;
import com.bodimtikka.service.AuthService;
import com.bodimtikka.security.JwtService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.registerUser(request.getName(), request.getEmail(), request.getPassword());

        List<String> roles = user.getAuth().getRolesList();
        String token = jwtService.generateToken(user.getId(), roles);

        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getName(), user.getEmail(), token));
    }

    /**
     * Login a user
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request.getEmail(), request.getPassword());

        List<String> roles = user.getAuth().getRolesList();
        String token = jwtService.generateToken(user.getId(), roles);

        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getName(), user.getEmail(), token));
    }
}
