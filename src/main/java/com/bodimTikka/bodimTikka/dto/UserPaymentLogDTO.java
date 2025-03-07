package com.bodimTikka.bodimTikka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPaymentLogDTO {
    private Long fromUser;
    private Long toUser;
    private BigDecimal Amount;
    private LocalDateTime paymentTimestamp;
    private String description;
}
