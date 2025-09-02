package com.bodimtikka.repository;

import com.bodimtikka.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Fetch all transactions by room ID
     */
    List<Transaction> findByRoomId(Long roomId);

    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.participants WHERE t.room.id = :roomId")
    List<Transaction> findAllByRoomIdWithParticipants(@Param("roomId") Long roomId);

    @Query("""
    SELECT DISTINCT t
    FROM Transaction t
    JOIN FETCH t.participants tp
    WHERE tp.participant.id = :participantId
""")
    List<Transaction> findAllByParticipantIdWithParticipants(@Param("participantId") Long participantId);
}
