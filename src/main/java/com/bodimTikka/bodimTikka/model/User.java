package com.bodimTikka.bodimTikka.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "\"user\"")  // Use double quotes for reserved keywords in PostgreSQL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;  // Nullable for unregistered users

    private String password;  // Nullable

    public User(String name) {
        this.name = name;
    }

    public User(Long userId) {
        this.id = userId;
    }
}
