package com.bodimTikka.bodimTikka.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class Payment {
    private Roomer payer;
    private List<Roomer> payedFor;
    private double amount;
    private String paymentDescription;
    private LocalDateTime paymentTimestamp;
}
