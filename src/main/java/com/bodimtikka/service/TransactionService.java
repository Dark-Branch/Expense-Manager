package com.bodimtikka.service;

import com.bodimtikka.dto.TransactionDTO;
import com.bodimtikka.dto.TransactionParticipantDTO;
import com.bodimtikka.dto.transaction.TransactionCreateRequest;
import com.bodimtikka.exception.ResourceNotFoundException;
import com.bodimtikka.model.*;
import com.bodimtikka.model.TransactionParticipant.Role;
import com.bodimtikka.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionParticipantRepository transactionParticipantRepository;
    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;

    /**
     * Create a transaction in a room with participants.
     */
    public Transaction createTransaction(Long roomId,
                                         String description,
                                         BigDecimal amount,
                                         List<TransactionCreateRequest.SenderDTO> senders,
                                         List<Long> receiverParticipantIds) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        validateParticipantsExistInRoom(senders, receiverParticipantIds, room);

        Transaction transaction = new Transaction();
        transaction.setRoom(room);
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setCreatedAt(LocalDateTime.now());

        Set<TransactionParticipant> participants = new HashSet<>();

        // --- Add senders ---
        BigDecimal totalSenderAmount = BigDecimal.ZERO;

        for (TransactionCreateRequest.SenderDTO senderReq : senders) {
            Participant sender = participantRepository.findById(senderReq.participantId())
                    .orElseThrow(() -> new IllegalArgumentException("Sender participant not found"));

            TransactionParticipant tp = new TransactionParticipant();
            tp.setParticipant(sender);
            tp.setRole(Role.SENDER);
            tp.setShareAmount(senderReq.amount());
            tp.setTransaction(transaction);

            participants.add(tp);
            totalSenderAmount = totalSenderAmount.add(senderReq.amount());
        }

        // --- Validate sender total matches transaction amount ---
        if (totalSenderAmount.compareTo(amount) != 0) {
            throw new IllegalArgumentException("Total sender amounts must equal transaction amount");
        }

        // --- Split amount among receivers ---
        if (receiverParticipantIds.isEmpty()) {
            throw new IllegalArgumentException("At least one receiver is required");
        }

        BigDecimal share = amount.divide(
                BigDecimal.valueOf(receiverParticipantIds.size()),
                2, RoundingMode.HALF_UP
        );

        for (Long receiverId : receiverParticipantIds) {
            Participant receiver = participantRepository.findById(receiverId)
                    .orElseThrow(() -> new IllegalArgumentException("Receiver participant not found"));

            TransactionParticipant tp = new TransactionParticipant();
            tp.setParticipant(receiver);
            tp.setRole(Role.RECEIVER);
            tp.setShareAmount(share);
            tp.setTransaction(transaction);

            participants.add(tp);
        }

        transaction.setParticipants(participants);

        // Save transaction + participants (cascade)
        return transactionRepository.save(transaction);
    }

    private static void validateParticipantsExistInRoom(List<TransactionCreateRequest.SenderDTO> senders, List<Long> receiverParticipantIds, Room room) {
        Set<Long> participantIds = room.getParticipants()
                .stream()
                .map(Participant::getId)
                .collect(Collectors.toSet());

        if (!participantIds.containsAll(receiverParticipantIds)) {
            throw new IllegalArgumentException("Some receivers are not part of this room");
        }

        if (!participantIds.containsAll(
                senders.stream().map(TransactionCreateRequest.SenderDTO::participantId).toList())) {
            throw new IllegalArgumentException("Some senders are not part of this room");
        }
    }

    // --- Create transaction and return DTO ---
    public TransactionDTO createTransactionDTO(TransactionCreateRequest request, Long userId) {
        // Ensure user is part of the room
        boolean isMember = participantRepository.existsByRoomIdAndUserIdAndIsStillAMemberTrue(request.roomId(), userId);
        if (!isMember) {
            throw new AccessDeniedException("You are not a member of this room");
        }

        Transaction transaction = createTransaction(
                request.roomId(),
                request.description(),
                request.amount(),
                request.senders(),
                request.receiverParticipantIds()
        );

        return mapToDTO(transaction);
    }

    /**
     * List all transactions in a room by room ID
     */
    public List<Transaction> getTransactionsByRoomId(Long roomId) {
        return transactionRepository.findByRoomId(roomId);
    }

    /**
     * List all transactions for a participant by participant ID
     */
    public List<TransactionParticipant> getTransactionsByParticipantId(Long participantId) {
        return transactionParticipantRepository.findByParticipantId(participantId);
    }

    // --- Fetch transactions by room ID and convert to DTOs ---
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsForRoom(Long roomId, Long userId) {
        // Ensure user is part of the room
        boolean isMember = participantRepository.existsByRoomIdAndUserIdAndIsStillAMemberTrue(roomId, userId);
        if (!isMember) {
            throw new AccessDeniedException("You are not a member of this room");
        }

        List<Transaction> transactions = transactionRepository.findAllByRoomIdWithParticipants(roomId);

        return transactions.stream()
                .map(this::mapToDTO)
                .toList();
    }

    // --- Fetch transactions for a participant by ID and convert to DTOs ---
    public List<TransactionDTO> getTransactionsByParticipantIdDTO(Long participantId) {
        return getTransactionsByParticipantId(participantId)
                .stream()
                .map(tp -> mapToDTO(tp.getTransaction()))
                .collect(Collectors.toList());
    }

    public TransactionDTO mapToDTO(Transaction transaction) {
        List<TransactionParticipantDTO> participantDTOs = transaction.getParticipants()
                .stream()
                .map(tp -> new TransactionParticipantDTO(
                        tp.getParticipant().getId(),
                        tp.getParticipant().getNickname(),
                        tp.getRole(),
                        tp.getShareAmount()
                ))
                .collect(Collectors.toList());

        return new TransactionDTO(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getCreatedAt(),
                transaction.getRoom().getId(),
                transaction.getRoom().getName(),
                participantDTOs
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsForParticipant(Long roomId, Long participantId, Long userId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        // Check that the participant belongs to the given room
        if (!participant.getRoom().getId().equals(roomId)) {
            throw new AccessDeniedException("Participant does not belong to this room");
        }

        // Check that participant belongs to logged-in user
        if (!participant.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only view your own transactions");
        }

        List<Transaction> transactions = transactionRepository.findAllByParticipantIdWithParticipants(participantId);

        return transactions.stream()
                .map(this::mapToDTO)
                .toList();
    }
}
