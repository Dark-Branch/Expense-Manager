package com.bodimTikka.bodimTikka.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomDTO {
    private Long id;
    private String name;

    public RoomDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
