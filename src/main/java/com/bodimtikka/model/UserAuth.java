package com.bodimtikka.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "user_auth")
public class UserAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "auth")
    private User user;

    private String passwordHash;
    private boolean isActive = true;
    private String roles; // e.g., "ROLE_USER,ROLE_ADMIN"

    private LocalDateTime lastLogin;

    public List<String> getRolesList() {
        if (roles == null || roles.isEmpty()) return Collections.emptyList();
        return Arrays.stream(roles.split(",")).map(String::trim).toList();
    }
}
