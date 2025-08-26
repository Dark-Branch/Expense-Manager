package com.bodimtikka.repository;

import com.bodimtikka.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Fetch all transactions by room ID
     */
    List<Transaction> findByRoomId(Long roomId);
}
