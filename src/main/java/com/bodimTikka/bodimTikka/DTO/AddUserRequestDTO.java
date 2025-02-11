package com.bodimTikka.bodimTikka.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddUserRequestDTO {
    private Long userId;
    private String name;
    private Boolean isRegistered;
}