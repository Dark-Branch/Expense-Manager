package com.bodimTikka.bodimTikka.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;


import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PaymentRequestDTO {

    @NotNull(message = "Room ID is required.")
    private Long roomId;

    @NotNull(message = "Total amount is required.")
    @Positive(message = "Total amount must be a positive value.")
    private BigDecimal totalAmount;

    @NotNull(message = "Repayment status is required.")
    private boolean isRepayment;

    @NotNull(message = "Payer ID is required.")
    private Long payerId;

    @NotEmpty(message = "Recipient IDs cannot be empty.")
    private List<Long> recipientIds;

    private String description;
}
