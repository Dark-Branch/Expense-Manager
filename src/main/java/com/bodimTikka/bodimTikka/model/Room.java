package com.bodimTikka.bodimTikka.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    public Room(String name) {
        this.name = name;
    }

    public Room(Long roomId) {
        this.id = roomId;
    }
}
