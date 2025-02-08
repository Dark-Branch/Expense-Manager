package com.bodimTikka.bodimTikka.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class PaymentRecordDTO {
    private Long fromUserId;
    private Long toUserId;
    private BigDecimal amount;
    private Boolean isCredit;
}
