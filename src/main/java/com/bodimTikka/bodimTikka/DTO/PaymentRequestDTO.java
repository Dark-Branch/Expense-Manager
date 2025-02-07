package com.bodimTikka.bodimTikka.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PaymentRequestDTO {
    private Long roomId;
    private BigDecimal totalAmount;
    private boolean isRepayment;
    private Long payerId;
    private List<Long> recipientIds;

}
