package com.bodimtikka.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;  // unique, used for login reference

    // Lazy fetch to prevent unnecessary joins when accessing User directly
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Participant> participants = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "auth_id")
    private UserAuth auth;
}
