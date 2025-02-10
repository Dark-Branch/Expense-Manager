package com.bodimTikka.bodimTikka.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomPaymentLogDTO {
    private UUID paymentId;
    private BigDecimal amount;
    private Long fromUserId;
    private LocalDateTime paymentTimestamp;
    private String description;
    private Boolean isRepayment;
    private List<Long> toUserIds;
}
