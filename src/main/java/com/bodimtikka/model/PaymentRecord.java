package com.bodimtikka.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payment_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "from_user_in_room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_paymentrecord_from_user"))
    private UserInRoom fromUser;

    @ManyToOne
    @JoinColumn(name = "to_user_in_room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_paymentrecord_to_user"))
    private UserInRoom toUser;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_paymentrecord_payment"))
    private Payment payment;
}
