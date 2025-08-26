package com.bodimtikka.repository;

import com.bodimtikka.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {

    // Find auth record by userId
    Optional<UserAuth> findByUserId(Long userId);

    // Optional: if you often authenticate directly with email
    Optional<UserAuth> findByUserEmail(String email);
}
