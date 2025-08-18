package com.bodimtikka.controller;

import com.bodimtikka.dto.JwtResponse;
import com.bodimtikka.dto.LoginRequest;
import com.bodimtikka.dto.SignupRequest;
import com.bodimtikka.model.User;
import com.bodimtikka.repository.UserRepository;
import com.bodimtikka.security.JwtUtils;
import com.bodimtikka.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        User registeredUser = authService.registerUser(signupRequest);
        return ResponseEntity.ok("User registered successfully: " + registeredUser.getEmail());
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        String jwt = authService.authenticateUser(loginRequest);

        return ResponseEntity.ok(new JwtResponse(jwt));
    }
}