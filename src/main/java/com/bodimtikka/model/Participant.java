package com.bodimtikka.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_rooms")
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The room they belong to, fetch lazily
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonBackReference
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    @JsonIgnore
    private User user;

    private String nickname;

    private LocalDateTime joinedAt = LocalDateTime.now();

    private boolean isStillAMember = true;

    public Participant() {}

    public Participant(User user, Room room, String nickname) {
        this.user = user;
        this.room = room;
        this.nickname = nickname;
    }

    public Participant(Room room, String nickname) {
        this.room = room;
        this.nickname = nickname;
    }
}
