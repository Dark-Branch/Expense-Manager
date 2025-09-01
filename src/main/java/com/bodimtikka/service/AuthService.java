package com.bodimtikka.service;

import com.bodimtikka.exception.ResourceAlreadyExistsException;
import com.bodimtikka.model.User;
import com.bodimtikka.model.UserAuth;
import com.bodimtikka.repository.UserAuthRepository;
import com.bodimtikka.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       UserAuthRepository userAuthRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userAuthRepository = userAuthRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user along with authentication details.
     */
    public User registerUser(String name, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Email");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);

        UserAuth auth = new UserAuth();
        auth.setPasswordHash(passwordEncoder.encode(rawPassword));
        auth.setRoles("ROLE_USER");
        auth.setActive(true);
        auth.setLastLogin(LocalDateTime.now());

        user.setAuth(auth);

        userRepository.save(user);  // Cascade ALL saves auth too

        return user;
    }

    /**
     * Authenticate a user by email and password
     */
    public User authenticate(String email, String rawPassword) {
        Optional<UserAuth> authOpt = userAuthRepository.findByUserEmail(email);

        if (authOpt.isEmpty() || !authOpt.get().isActive()) {
            throw new DisabledException("Invalid credentials or inactive account");
        }

        UserAuth auth = authOpt.get();

        if (!passwordEncoder.matches(rawPassword, auth.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Update last login
        auth.setLastLogin(LocalDateTime.now());
        userAuthRepository.save(auth);

        return auth.getUser();
    }

    /**
     * Update user password
     */
    public void updatePassword(Long userId, String newRawPassword) {
        UserAuth auth = userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User auth not found"));

        if (newRawPassword == null || newRawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        auth.setPasswordHash(passwordEncoder.encode(newRawPassword));
        userAuthRepository.save(auth);
    }

}
