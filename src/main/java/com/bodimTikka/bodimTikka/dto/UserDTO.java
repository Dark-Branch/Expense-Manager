package com.bodimTikka.bodimTikka.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserDTO {
    private UUID id;
    private String name;
    private String email;

    public UserDTO(UUID id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
