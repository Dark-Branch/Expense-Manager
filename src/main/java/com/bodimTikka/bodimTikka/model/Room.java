package com.bodimTikka.bodimTikka.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable = false)
    private String name;

    public Room(String name) {
        this.name = name;
    }

    public Room(UUID roomId) {
        this.id = roomId;
    }
}
