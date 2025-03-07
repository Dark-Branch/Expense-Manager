package com.bodimTikka.bodimTikka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddUserRequestDTO {
    private UUID userId;
    private String name;
    private Boolean isRegistered;
}