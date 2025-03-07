package com.bodimTikka.bodimTikka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PaymentResponseDTO {
    private UUID paymentId;
    private UUID roomId;
    private BigDecimal amount;
    private Boolean isRepayment;
    private LocalDateTime paymentTimestamp;
    private String description;
    private List<PaymentRecordDTO> paymentRecords;
}
