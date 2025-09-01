package com.bodimtikka.dto.userroom;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddMemberRequest {
    private Long userId;

    @NotBlank(message = "Nickname is required")
    private String nickname;
}
