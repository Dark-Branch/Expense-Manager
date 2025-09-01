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

    // --- Room Owner ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // --- Room Members ---
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<UserRoom> userRooms = new HashSet<>();

    // --- Transactions in the room ---
    @OneToMany(mappedBy = "room")
    private Set<Transaction> transactions = new HashSet<>();

    public Room(String name) {
        this.name = name;
    }

    public Room() {
    }
}
