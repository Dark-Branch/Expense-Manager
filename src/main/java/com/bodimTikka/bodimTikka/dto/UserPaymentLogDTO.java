package com.bodimTikka.bodimTikka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPaymentLogDTO {
    private UUID fromUser;
    private UUID toUser;
    private BigDecimal Amount;
    private LocalDateTime paymentTimestamp;
    private String description;
}
