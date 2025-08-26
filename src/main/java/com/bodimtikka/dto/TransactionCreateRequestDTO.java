package com.bodimtikka.dto;

import java.math.BigDecimal;
import java.util.List;

public record TransactionCreateRequestDTO(
        Long roomId,
        String description,
        BigDecimal amount,
        List<Long> senderParticipantIds,
        List<BigDecimal> senderAmounts,
        List<Long> receiverParticipantIds,
        List<BigDecimal> receiverAmounts
) {}
