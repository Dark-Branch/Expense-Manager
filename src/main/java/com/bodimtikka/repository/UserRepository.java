package com.bodimtikka.repository;

import com.bodimtikka.dto.UserDTO;
import com.bodimtikka.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT new com.bodimtikka.dto.UserDTO(u.id, u.name, u.email) FROM User u WHERE u.email = :email")
    Optional<UserDTO> findUserProjectionByEmail(@Param("email") String email);

    @Query("SELECT new com.bodimtikka.dto.UserDTO(u.id, u.name, u.email) FROM User u WHERE u.name = :name")
    Optional<UserDTO> findUserProjectionByName(@Param("name") String name);

    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.email = :email")
    void removeByEmail(@Param("email") String email);

    UserDTO getUserByEmail(String email);
}
