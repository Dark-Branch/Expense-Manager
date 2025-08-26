package com.bodimtikka.service;

import com.bodimtikka.model.User;
import com.bodimtikka.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get user by ID
     */
    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Optional<User> getUserByIdOptional(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Search users by name (partial match, case-insensitive)
     */
    public List<User> searchUsersByName(String keyword) {
        return userRepository.findByNameContainingIgnoreCase(keyword);
    }

    /**
     * Update user details
     */
    public User updateUser(Long userId, String newName, String newEmail) {
        User user = getUserById(userId);

        // Optional: check for email uniqueness
        if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Email already in use");
        }

        user.setName(newName);
        user.setEmail(newEmail);

        return userRepository.save(user);
    }

    /**
     * Delete a user
     */
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }
}
