package com.bodimTikka.bodimTikka.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payment_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_paymentlog_from_user"))
    private User fromUser;

    @ManyToOne
    @JoinColumn(name = "to_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_paymentlog_to_user"))
    private User toUser;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false, foreignKey = @ForeignKey(name = "fk_paymentlog_transaction"))
    private Transaction transaction;
}
