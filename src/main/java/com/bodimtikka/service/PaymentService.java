package com.bodimtikka.service;

import com.bodimtikka.dto.*;
import com.bodimtikka.exceptions.InvalidRequestException;
import com.bodimtikka.exceptions.NotFoundException;
import com.bodimtikka.exceptions.UnauthorizedException;
import com.bodimtikka.model.*;
import com.bodimtikka.repository.PaymentRepository;
import com.bodimtikka.repository.PaymentRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static com.bodimtikka.dto.RoomPairBalanceDTO.getRoomPairBalanceDTOS;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final RoomService roomService;
    private final UserService userService;

    public PaymentService(PaymentRepository paymentRepository, PaymentRecordRepository paymentRecordRepository, RoomService roomService, UserService userService) {
        this.paymentRepository = paymentRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.roomService = roomService;
        this.userService = userService;
    }

    // for testing
    public Payment getById(UUID id){
        return paymentRepository.findById(id).orElseThrow(() -> new NotFoundException("Payment Not Found"));
    }

    // TODO: make this transaction small
    @Transactional
    public Payment createPayment(PaymentRequestDTO paymentRequest, String principalEmail) {
        UUID principalId = userService.getUserByEmail(principalEmail).getId();
        Room room = getRoomOrElseThrow(paymentRequest.getRoomId());

        verifyPrincipalInRoom(principalId, room.getId(), "Cannot add payments to room you are not a member of");
        List<UUID> userIDs = roomService.getUserInRoomIDs(room.getId());

        UserInRoom payer = getPayer(paymentRequest, userIDs);
        List<UserInRoom> recipients = getRecipients(paymentRequest, userIDs);

        Payment payment = makePayment(paymentRequest, room);
        BigDecimal splitAmount = getSplitPaymentAmount(paymentRequest, recipients);

        createPaymentRecords(recipients, payer, splitAmount, payment);
        return payment;
    }

    private void verifyPrincipalInRoom(UUID principalId, UUID roomId, String errorMessage) {
        if (!roomService.isUserInRoom(principalId, roomId)){
            throw new UnauthorizedException(errorMessage);
        }
    }

    private void createPaymentRecords(List<UserInRoom> recipients, UserInRoom payer, BigDecimal splitAmount, Payment payment) {
        recipients.forEach(recipient -> {
            createPaymentRecord(payer, recipient, splitAmount, payment);
        });
    }

    // TODO: no need to pass all array just to get size
    private static BigDecimal getSplitPaymentAmount(PaymentRequestDTO paymentRequest, List<UserInRoom> recipients) {
        BigDecimal splitAmount;
        if (!paymentRequest.isRepayment()) {
            // TODO: rounding is business decision, so? decide
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
        // TODO: do we need this
        payment.setIsRepayment(paymentRequest.isRepayment());
        payment.setDescription(paymentRequest.getDescription());
        payment = paymentRepository.save(payment);
        return payment;
    }

    private static UserInRoom getPayer(PaymentRequestDTO paymentRequest, List<UUID> userInRoomIDs) {
        UserInRoom payer = new UserInRoom();
        payer.setId(paymentRequest.getPayerId());
        verifyContains(payer.getId(), userInRoomIDs, "payer Id does not belong to room");
        return payer;
    }

    private static List<UserInRoom> getRecipients(PaymentRequestDTO paymentRequest, List<UUID> userInRoomIDs) {
        return paymentRequest.getRecipientIds().stream().map(id -> {
            verifyContains(id, userInRoomIDs, "Recipient Id does not belong to room");
            UserInRoom userInRoom = new UserInRoom();
            userInRoom.setId(id);
            return userInRoom;
        }).toList();
    }

    public List<Payment> getPaymentByRoomId(Long roomId, int limit, int page){
        Pageable pageable = PageRequest.of(page, limit);
        return paymentRepository.findLastPaymentsByRoomId(roomId, pageable);

    }

    private static void verifyContains(UUID value, List<UUID> target, String message) {
        if (!target.contains(value))
            throw new InvalidRequestException(message);
    }

    public List<UserPaymentLogDTO> getPaymentByRoomIdAndUsers(UUID roomId, UUID userId1, UUID userId2, int limit, int page, String principalEmail) {
        UUID principalId = userService.getUserByEmail(principalEmail).getId();
        // TODO: can refactor this bcuz there is no use of room anymore
        Room room = getRoomOrElseThrow(roomId);

        verifyPrincipalInRoom(principalId, roomId, "Cannot view payments for a room you are not a member of");
        List<UUID> userIds = roomService.getUserInRoomIDs(roomId);

        if (!new HashSet<>(userIds).containsAll(Arrays.asList(userId1, userId2))) {
            throw new InvalidRequestException("Users do not belong to the room");
        }

        if (page < 0 || limit <= 0) {
            throw new InvalidRequestException("Invalid pagination parameters");
        }

        Pageable pageable = PageRequest.of(page, limit);
        return paymentRepository.findLastPaymentsByRoomIdAndUsers(roomId, userId1, userId2, pageable);
    }

    private Room getRoomOrElseThrow(UUID roomId) {
        return roomService.getRoomById(roomId).orElseThrow(() -> new
                NotFoundException("Room not found"));
    }

    private void createPaymentRecord(UserInRoom payer, UserInRoom recipient, BigDecimal amount, Payment payment) {
        PaymentRecord record = new PaymentRecord();
        record.setFromUser(payer);
        record.setToUser(recipient);
        record.setAmount(amount);
        record.setPayment(payment);
        paymentRecordRepository.save(record);
    }

    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll().stream().map(payment -> {
            List<PaymentRecordDTO> records = paymentRecordRepository.findAll()
                    .stream()
                    .filter(record -> record.getPayment().getPaymentId().equals(payment.getPaymentId()))
                    .map(record -> new PaymentRecordDTO(
                            record.getFromUser().getId(),
                            record.getToUser().getId(),
                            record.getAmount()))
                    .collect(Collectors.toList());

            return getPaymentResponseDTO(payment, records);
        }).collect(Collectors.toList());
    }

    // TODO: check both users are same
    private static PaymentResponseDTO getPaymentResponseDTO(Payment payment, List<PaymentRecordDTO> paymentRecords) {
        return new PaymentResponseDTO(
                payment.getPaymentId(),
                payment.getRoom().getId(),
                payment.getAmount(),
                payment.getIsRepayment(),
                payment.getPaymentTimestamp(),
                payment.getDescription(),
                paymentRecords
        );
    }

    public List<RoomPaymentLogDTO> getLastRoomPayments(UUID roomId, int limit, int page, String principalEmail) {
        UUID principalId = userService.getUserByEmail(principalEmail).getId();
        Room room = getRoomOrElseThrow(roomId);

        verifyPrincipalInRoom(principalId, roomId, "Cannot view payments for a room you are not a member of");

        // native query, so no pageable
        int offset = page * limit;
        List<Object[]> results = paymentRepository.findLastRoomPayments(roomId, limit, offset);

        return results.stream().map(row -> new RoomPaymentLogDTO(
                (UUID) row[0],                           // paymentId
                (BigDecimal) row[1],                     // amount
                (UUID) row[2],           // fromUserId
                // special for psql
                ((Timestamp) row[3]).toLocalDateTime(),  // Timestamp
                (String) row[4],                         // description
                (boolean) row[5],                        // isRepayment
                row[6] != null ? Arrays.stream(((String) row[6]).split("/"))  // toUserIds (split string)
                        .map(UUID::fromString)
                        .collect(Collectors.toList()) : Collections.emptyList()
        )).collect(Collectors.toList());
    }

    @Transactional
    public List<RoomPairBalanceDTO> getPairwiseBalances(UUID roomId) {
        Room room = getRoomOrElseThrow(roomId);
        // crucial because materialized view
        paymentRecordRepository.refreshMaterializedView();

        List<Object[]> results = paymentRecordRepository.findPairwiseBalancesByRoom(roomId);
        return getRoomPairBalanceDTOS(results);
    }
}
