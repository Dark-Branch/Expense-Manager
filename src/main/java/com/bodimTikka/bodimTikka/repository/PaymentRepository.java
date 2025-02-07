package com.bodimTikka.bodimTikka.repository;

import com.bodimTikka.bodimTikka.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
