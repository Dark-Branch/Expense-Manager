package com.bodimtikka.service;

import com.bodimtikka.dto.TransactionDTO;
import com.bodimtikka.dto.TransactionParticipantDTO;
import com.bodimtikka.model.*;
import com.bodimtikka.model.TransactionParticipant.Role;
import com.bodimtikka.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final UserRoomRepository userRoomRepository;

    /**
     * Create a transaction in a room with participants.
     */
    public Transaction createTransaction(Long roomId,
                                         String description,
                                         BigDecimal amount,
                                         List<Long> senderParticipantIds,
                                         List<Long> receiverParticipantIds,
                                         List<BigDecimal> senderAmounts,
                                         List<BigDecimal> receiverAmounts) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        Transaction transaction = new Transaction();
        transaction.setRoom(room);
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setCreatedAt(LocalDateTime.now());

        Set<TransactionParticipant> participants = new HashSet<>();

        // Add senders
        for (int i = 0; i < senderParticipantIds.size(); i++) {
            UserRoom sender = userRoomRepository.findById(senderParticipantIds.get(i))
                    .orElseThrow(() -> new IllegalArgumentException("Sender participant not found"));

            TransactionParticipant tp = new TransactionParticipant();
            tp.setParticipant(sender);
            tp.setRole(Role.SENDER);
            tp.setShareAmount(senderAmounts.get(i));
            tp.setTransaction(transaction);

            participants.add(tp);
        }

        // Add receivers
        for (int i = 0; i < receiverParticipantIds.size(); i++) {
            UserRoom receiver = userRoomRepository.findById(receiverParticipantIds.get(i))
                    .orElseThrow(() -> new IllegalArgumentException("Receiver participant not found"));

            TransactionParticipant tp = new TransactionParticipant();
            tp.setParticipant(receiver);
            tp.setRole(Role.RECEIVER);
            tp.setShareAmount(receiverAmounts.get(i));
            tp.setTransaction(transaction);

            participants.add(tp);
        }

        transaction.setParticipants(participants);

        // Save transaction + participants (cascade)
        return transactionRepository.save(transaction);
    }

    // --- Create transaction and return DTO ---
    public TransactionDTO createTransactionDTO(Long roomId,
                                               String description,
                                               BigDecimal amount,
                                               List<Long> senderParticipantIds,
                                               List<Long> receiverParticipantIds,
                                               List<BigDecimal> senderAmounts,
                                               List<BigDecimal> receiverAmounts) {
        Transaction transaction = createTransaction(
                roomId, description, amount,
                senderParticipantIds, receiverParticipantIds,
                senderAmounts, receiverAmounts
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
    public List<TransactionDTO> getTransactionsByRoomIdDTO(Long roomId) {
        return getTransactionsByRoomId(roomId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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
}
