package com.bodimTikka.bodimTikka.controller;

import com.bodimTikka.bodimTikka.DTO.PaymentRequestDTO;
import com.bodimTikka.bodimTikka.DTO.PaymentResponseDTO;
import com.bodimTikka.bodimTikka.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentRequestDTO paymentRequest) {
        PaymentResponseDTO paymentResponse = paymentService.createPayment(paymentRequest);
        return ResponseEntity.ok(paymentResponse);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }
}
