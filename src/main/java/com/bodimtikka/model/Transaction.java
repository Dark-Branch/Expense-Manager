package com.bodimtikka.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private BigDecimal amount; // total amount of the transaction

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    // Links to users as senders/receivers
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL)
    private Set<TransactionParticipant> participants;
}
