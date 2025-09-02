package com.bodimtikka.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

public record TransactionCreateRequest(

        @NotNull(message = "Room ID is required")
        Long roomId,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotEmpty(message = "At least one sender is required")
        @Valid
        List<SenderDTO> senders,

        @NotEmpty(message = "At least one receiver is required")
        List<@NotNull(message = "Receiver participant ID cannot be null") Long> receiverParticipantIds
) {
    public record SenderDTO(

            @NotNull(message = "Sender participant ID is required")
            Long participantId,

            @NotNull(message = "Sender amount is required")
            @Positive(message = "Sender amount must be greater than 0")
            BigDecimal amount
    ) {}
}
