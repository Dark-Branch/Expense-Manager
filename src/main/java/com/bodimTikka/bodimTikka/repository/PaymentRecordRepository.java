package com.bodimTikka.bodimTikka.repository;

import com.bodimTikka.bodimTikka.model.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
}
