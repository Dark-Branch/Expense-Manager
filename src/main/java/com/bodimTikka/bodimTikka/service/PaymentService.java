package com.bodimTikka.bodimTikka.service;

import com.bodimTikka.bodimTikka.DTO.PaymentRequestDTO;
import com.bodimTikka.bodimTikka.DTO.PaymentResponseDTO;
import com.bodimTikka.bodimTikka.DTO.PaymentRecordDTO;
import com.bodimTikka.bodimTikka.model.Payment;
import com.bodimTikka.bodimTikka.model.PaymentRecord;
import com.bodimTikka.bodimTikka.model.Room;
import com.bodimTikka.bodimTikka.model.User;
import com.bodimTikka.bodimTikka.repository.PaymentRepository;
import com.bodimTikka.bodimTikka.repository.PaymentRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    public PaymentService(PaymentRepository paymentRepository, PaymentRecordRepository paymentRecordRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentRecordRepository = paymentRecordRepository;
    }

    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO paymentRequest) {
        // Simulate fetching Room and User objects
        // TODO
        Room room = new Room();
        room.setId(paymentRequest.getRoomId());

        User payer = new User();
        payer.setId(paymentRequest.getPayerId());

        // TODO: move this to user service, also need to verify
        List<User> recipients = paymentRequest.getRecipientIds().stream().map(id -> {
            User user = new User();
            user.setId(id);
            return user;
        }).toList();

        Payment payment = new Payment();
        payment.setRoom(room);
        payment.setAmount(paymentRequest.getTotalAmount());
        payment.setIsRepayment(paymentRequest.isRepayment());
        payment = paymentRepository.save(payment);

        List<PaymentRecordDTO> paymentRecords;

        BigDecimal splitAmount;
        if (!paymentRequest.isRepayment()) {
            splitAmount = paymentRequest.getTotalAmount().divide(BigDecimal.valueOf(recipients.size()), RoundingMode.HALF_UP);
        } else {
            splitAmount = paymentRequest.getTotalAmount();
        }

        Payment finalPayment = payment;
        paymentRecords = recipients.stream()
                .map(recipient -> {
                    PaymentRecord record = createPaymentRecord(payer, recipient, splitAmount, finalPayment);
                    return new PaymentRecordDTO(record.getFromUser().getId(), record.getToUser().getId(), splitAmount, record.getIsCredit());
                })
                .collect(Collectors.toList());

        return new PaymentResponseDTO(
                payment.getPaymentId(),
                payment.getRoom().getId(),
                payment.getAmount(),
                payment.getIsRepayment(),
                payment.getPaymentTimestamp(),
                paymentRecords
        );
    }

    private PaymentRecord createPaymentRecord(User payer, User recipient, BigDecimal amount, Payment payment) {
        PaymentRecord record = new PaymentRecord();

        // if always transaction goes from low user to high id user
        if (recipient.getId() < payer.getId()) {
            record.setFromUser(recipient);
            record.setToUser(payer);
            record.setIsCredit(true);
        } else {
            record.setFromUser(payer);
            record.setToUser(recipient);
            record.setIsCredit(false);
        }

        record.setAmount(amount);
        record.setPayment(payment);
        return paymentRecordRepository.save(record);
    }

    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll().stream().map(payment -> {
            List<PaymentRecordDTO> records = paymentRecordRepository.findAll()
                    .stream()
                    .filter(record -> record.getPayment().getPaymentId().equals(payment.getPaymentId()))
                    .map(record -> new PaymentRecordDTO(
                            record.getFromUser().getId(),
                            record.getToUser().getId(),
                            record.getAmount(),
                            record.getIsCredit()))
                    .collect(Collectors.toList());

            return new PaymentResponseDTO(
                    payment.getPaymentId(),
                    payment.getRoom().getId(),
                    payment.getAmount(),
                    payment.getIsRepayment(),
                    payment.getPaymentTimestamp(),
                    records
            );
        }).collect(Collectors.toList());
    }
}
