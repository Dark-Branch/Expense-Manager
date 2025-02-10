package com.bodimTikka.bodimTikka.service;

import com.bodimTikka.bodimTikka.DTO.*;
import com.bodimTikka.bodimTikka.exceptions.InvalidArgumentException;
import com.bodimTikka.bodimTikka.exceptions.InvalidPaymentException;
import com.bodimTikka.bodimTikka.exceptions.NotFoundException;
import com.bodimTikka.bodimTikka.model.Payment;
import com.bodimTikka.bodimTikka.model.PaymentRecord;
import com.bodimTikka.bodimTikka.model.Room;
import com.bodimTikka.bodimTikka.model.User;
import com.bodimTikka.bodimTikka.repository.PaymentRepository;
import com.bodimTikka.bodimTikka.repository.PaymentRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final RoomService roomService;

    public PaymentService(PaymentRepository paymentRepository, PaymentRecordRepository paymentRecordRepository, RoomService roomService) {
        this.paymentRepository = paymentRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.roomService = roomService;
    }

    // for testing
    public Payment getById(UUID id){
        return paymentRepository.findById(id).orElseThrow(() -> new NotFoundException("Payment Not Found"));
    }

    @Transactional
    public Payment createPayment(PaymentRequestDTO paymentRequest) {
        Room room = roomService.getRoomById(paymentRequest.getRoomId()).orElseThrow(() -> new
                NotFoundException("Room not found"));

        List<Long> userIDs = roomService.getRoomUserIDs(room.getId());
        User payer = getPayer(paymentRequest, userIDs);
        List<User> recipients = getRecipients(paymentRequest, userIDs);

        Payment payment = makePayment(paymentRequest, room);
        BigDecimal splitAmount = getSplitPaymentAmount(paymentRequest, recipients);

        createPaymentRecords(recipients, payer, splitAmount, payment);
        return payment;
    }

    private void createPaymentRecords(List<User> recipients, User payer, BigDecimal splitAmount, Payment payment) {
        recipients.forEach(recipient -> {
            createPaymentRecord(payer, recipient, splitAmount, payment);
        });
    }

    private static BigDecimal getSplitPaymentAmount(PaymentRequestDTO paymentRequest, List<User> recipients) {
        BigDecimal splitAmount;
        if (!paymentRequest.isRepayment()) {
            splitAmount = paymentRequest.getTotalAmount().divide(BigDecimal.valueOf(recipients.size()), RoundingMode.HALF_UP);
        } else {
            splitAmount = paymentRequest.getTotalAmount();
        }
        return splitAmount;
    }

    private Payment makePayment(PaymentRequestDTO paymentRequest, Room room) {
        Payment payment = new Payment();
        payment.setRoom(room);
        payment.setAmount(paymentRequest.getTotalAmount());
        payment.setIsRepayment(paymentRequest.isRepayment());
        payment = paymentRepository.save(payment);
        return payment;
    }

    private static User getPayer(PaymentRequestDTO paymentRequest, List<Long> userIDs) {
        User payer = new User();
        payer.setId(paymentRequest.getPayerId());
        verifyContains(payer.getId(), userIDs, "payer Id does not belong to room");
        return payer;
    }

    private static List<User> getRecipients(PaymentRequestDTO paymentRequest, List<Long> userIDs) {
        return paymentRequest.getRecipientIds().stream().map(id -> {
            verifyContains(id, userIDs, "Recipient Id does not belong to room");
            User user = new User();
            user.setId(id);
            return user;
        }).toList();
    }

    private static void verifyContains(Long value, List<Long> target, String message) {
        if (!target.contains(value))
            throw new InvalidArgumentException(message);
    }

    public List<Payment> getPaymentByRoomId(Long roomId, int limit, int page){
        Pageable pageable = PageRequest.of(page, limit);
        return paymentRepository.findLastPaymentsByRoomId(roomId, pageable);
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

            return getPaymentResponseDTO(payment, records);
        }).collect(Collectors.toList());
    }
}
