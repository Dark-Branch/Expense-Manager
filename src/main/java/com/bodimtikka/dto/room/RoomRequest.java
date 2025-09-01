package com.bodimtikka.dto.room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
public class RoomRequest {
    @NotBlank(message = "Room name must not be empty")
    private String name;
}
