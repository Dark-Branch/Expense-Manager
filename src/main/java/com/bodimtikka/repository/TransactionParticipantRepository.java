package com.bodimtikka.repository;

import com.bodimtikka.model.TransactionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionParticipantRepository extends JpaRepository<TransactionParticipant, Long> {

    /**
     * Fetch all participants by participant ID
     */
    List<TransactionParticipant> findByParticipantId(Long participantId);

    /**
     * Fetch all participants by transaction ID
     */
    List<TransactionParticipant> findByTransactionId(Long transactionId);
}
