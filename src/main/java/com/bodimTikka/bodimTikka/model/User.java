package com.bodimTikka.bodimTikka.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "\"user\"")  // Use double quotes for reserved keywords in PostgreSQL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;  // Nullable for unregistered users

    private String password;  // Nullable

    public User(String name) {
        this.name = name;
    }

    public User(UUID userId) {
        this.id = userId;
    }
}
