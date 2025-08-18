package com.bodimtikka.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RoomDTO {
    private UUID id;
    private String name;

    public RoomDTO(UUID id, String name) {
        this.id = id;
        this.name = name;
    }
}
