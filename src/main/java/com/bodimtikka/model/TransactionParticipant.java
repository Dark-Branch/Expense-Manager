package com.bodimtikka.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Entity
@Table(name = "transaction_participants")
public class TransactionParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user in this transaction
    @ManyToOne
    @JoinColumn(name = "participant_id")
    private Participant participant;

    // transaction reference
    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    private Role role;

    private BigDecimal shareAmount; // how much this user sent/received

    public enum Role {
        SENDER,
        RECEIVER
    }
}
