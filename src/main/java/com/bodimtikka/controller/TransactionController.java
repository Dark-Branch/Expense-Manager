package com.bodimtikka.controller;

import com.bodimtikka.dto.transaction.TransactionCreateRequest;
import com.bodimtikka.dto.TransactionDTO;
import com.bodimtikka.security.JwtUserPrincipal;
import com.bodimtikka.service.TransactionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@AllArgsConstructor
@RestController
@RequestMapping("/api/rooms/{roomId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    // --- Create transaction ---
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(
            @PathVariable Long roomId,
            @Valid @RequestBody TransactionCreateRequest request,
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        TransactionDTO dto = transactionService.createTransactionDTO(
                new TransactionCreateRequest(
                        roomId,
                        request.description(),
                        request.amount(),
                        request.senders(),
                        request.receiverParticipantIds()
                ),
                principal.id() // pass the logged-in user
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }


    // --- List transactions in a room ---
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TransactionDTO>> getRoomTransactions(
            @PathVariable Long roomId,
            @AuthenticationPrincipal JwtUserPrincipal principal) {

        List<TransactionDTO> transactions = transactionService.getTransactionsForRoom(roomId, principal.id());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/participant/{participantId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TransactionDTO>> getTransactionsForParticipant(
            @PathVariable Long roomId,
            @PathVariable Long participantId,
            @AuthenticationPrincipal JwtUserPrincipal principal) {

        List<TransactionDTO> transactions = transactionService.getTransactionsForParticipant(roomId, participantId, principal.id());
        return ResponseEntity.ok(transactions);
    }
}
