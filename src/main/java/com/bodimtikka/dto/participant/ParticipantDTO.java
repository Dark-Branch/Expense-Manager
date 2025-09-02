package com.bodimtikka.dto.participant;

import com.bodimtikka.dto.RoomDTO;

public record ParticipantDTO(
        Long userId,
        RoomDTO room,
        String nickname
) {}
