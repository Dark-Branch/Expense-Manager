package com.bodimtikka.dto.participant;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddParticipantRequest {
    private Long userId;

    @NotBlank(message = "Nickname is required")
    private String nickname;
}
