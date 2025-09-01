package com.bodimtikka.dto.userroom;

import com.bodimtikka.dto.RoomDTO;

public record UserRoomDTO(
        Long userId,
        RoomDTO room,
        String nickname
) {}
