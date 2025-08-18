package com.bodimtikka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PaymentRecordDTO {
    private UUID fromUserId;
    private UUID toUserId;
    private BigDecimal amount;
}
