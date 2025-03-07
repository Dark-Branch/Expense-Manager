package com.bodimTikka.bodimTikka.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserInRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", referencedColumnName = "id")
    private Room room;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_still_a_member")
    private boolean isStillAMember;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false;

    @Column(nullable = false)
    private boolean isRegistered = false;

    public UserInRoom(User user, Room room, String name) {
        this.user = user;
        this.room = room;
        this.name = name;
        this.isStillAMember = true;
    }
}
