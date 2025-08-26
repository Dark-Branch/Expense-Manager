package com.bodimtikka.dto;

import com.bodimtikka.model.TransactionParticipant;

import java.math.BigDecimal;

public record TransactionParticipantDTO(
        Long participantId,
        String participantName,
        TransactionParticipant.Role role,
        BigDecimal shareAmount
) {}
