package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.model.Payment;
import com.bodimTikka.bodimTikka.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        Payment createdPayment = paymentService.createPayment(payment);
        return new ResponseEntity<>(createdPayment, HttpStatus.CREATED);
    }

    @GetMapping("/payer/{payerId}")
    public ResponseEntity<List<Payment>> getPaymentsByPayer(@PathVariable Long payerId) {
        List<Payment> payments = paymentService.getPaymentsByPayer(payerId);
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    @GetMapping("/roomer/{roomerId}")
    public ResponseEntity<List<Payment>> getPaymentsForRoomer(@PathVariable Long roomerId) {
        List<Payment> payments = paymentService.getPaymentsForRoomer(roomerId);
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }
}
