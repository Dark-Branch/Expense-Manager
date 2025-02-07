package com.bodimTikka.bodimTikka.repository;

import com.bodimTikka.bodimTikka.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPayer_Id(Long payerId);

    List<Payment> findByBeneficiaries_Id(Long roomerId);
}
