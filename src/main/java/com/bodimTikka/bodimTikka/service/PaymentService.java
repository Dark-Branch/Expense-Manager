package com.bodimTikka.bodimTikka.service;

import com.bodimTikka.bodimTikka.model.Payment;
import com.bodimTikka.bodimTikka.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment createPayment(Payment payment) {
        payment.setPaymentTimestamp(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public List<Payment> getPaymentsByPayer(Long payerId) {
        return paymentRepository.findByPayer_Id(payerId);
    }

    public List<Payment> getPaymentsForRoomer(Long roomerId) {
        return paymentRepository.findByBeneficiaries_Id(roomerId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}
