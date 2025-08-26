package com.bodimtikka.repository;

import com.bodimtikka.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find by email (for login/auth)
    Optional<User> findByEmail(String email);

    // Check existence by email (useful for signup validation)
    boolean existsByEmail(String email);

    // Search by name (case-insensitive, partial match)
    List<User> findByNameContainingIgnoreCase(String keyword);
}
