package com.bodimtikka.controller;

import com.bodimtikka.dto.auth.LoginRequest;
import com.bodimtikka.dto.auth.RegisterRequest;
import com.bodimtikka.dto.auth.UserResponse;
import com.bodimtikka.model.User;
import com.bodimtikka.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.registerUser(request.getName(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getName(), user.getEmail()));
    }

    /**
     * Login a user
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getName(), user.getEmail()));
    }
}
