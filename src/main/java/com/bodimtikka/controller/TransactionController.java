package com.bodimtikka.controller;

import com.bodimtikka.dto.TransactionCreateRequestDTO;
import com.bodimtikka.dto.TransactionDTO;
import com.bodimtikka.service.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // --- Create transaction ---
    @PostMapping("/create")
    public TransactionDTO createTransaction(@RequestBody TransactionCreateRequestDTO request) {
        return transactionService.createTransactionDTO(
                request.roomId(),
                request.description(),
                request.amount(),
                request.senderParticipantIds(),
                request.receiverParticipantIds(),
                request.senderAmounts(),
                request.receiverAmounts()
        );
    }

    // --- List transactions in a room ---
    @GetMapping("/room/{roomId}")
    public List<TransactionDTO> getTransactionsByRoom(@PathVariable Long roomId) {
        return transactionService.getTransactionsByRoomIdDTO(roomId);
    }

    // --- List transactions for a participant ---
    @GetMapping("/participant/{participantId}")
    public List<TransactionDTO> getTransactionsByParticipant(@PathVariable Long participantId) {
        return transactionService.getTransactionsByParticipantIdDTO(participantId);
    }
}
