package com.bodimTikka.bodimTikka.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BackPayment {
    private Roomer rePayer;
    private Roomer receiver;
    private double amount;
}
