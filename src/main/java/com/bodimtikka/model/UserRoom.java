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
public class UserRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many participants per room, fetch lazily to avoid loading User/Participant details unnecessarily
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    // The room they belong to, fetch lazily
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonBackReference
    private Room room;

    private String nickname;

    private LocalDateTime joinedAt = LocalDateTime.now();

    private boolean isStillAMember;

    public UserRoom() {}

    public UserRoom(Participant participant, Room room, String nickname) {
        this.participant = participant;
        this.room = room;
        this.nickname = nickname;
    }
}
