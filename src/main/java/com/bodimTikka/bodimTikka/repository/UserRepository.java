package com.bodimTikka.bodimTikka.repository;

import com.bodimTikka.bodimTikka.DTO.UserProjection;
import com.bodimTikka.bodimTikka.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u.id AS id, u.name AS name FROM User u WHERE u.email = :email")
    Optional<UserProjection> findUserProjectionByEmail(@Param("email") String email);
}
