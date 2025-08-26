package com.bodimtikka.dto;

import java.time.LocalDateTime;

public record UserRoomDTO(
        Long id,
        ParticipantDTO participant,
        RoomDTO room,
        String nickname,
        boolean stillAMember,
        LocalDateTime joinedAt
) {}
