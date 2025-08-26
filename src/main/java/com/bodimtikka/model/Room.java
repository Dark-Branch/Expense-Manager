package com.bodimtikka.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Link to UserRoom
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<UserRoom> userRooms = new HashSet<>();

    @OneToMany(mappedBy = "room")
    private Set<Transaction> transactions;

    public Room(String name) {
        this.name = name;
    }

    public Room() {
    }
}
