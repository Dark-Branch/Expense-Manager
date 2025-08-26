package com.bodimtikka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TransactionDTO(
        Long transactionId,
        String description,
        BigDecimal amount,
        LocalDateTime createdAt,
        Long roomId,
        String roomName,
        List<TransactionParticipantDTO> participants
) {}
