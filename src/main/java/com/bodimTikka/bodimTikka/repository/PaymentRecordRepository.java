package com.bodimTikka.bodimTikka.repository;

import com.bodimTikka.bodimTikka.model.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, UUID> {
    @Query(value = """
    SELECT room_id, from_user, to_user, balance
    FROM room_pair_balances_to_pay
    WHERE room_id = :roomId AND balance <> 0
    ORDER BY balance DESC;
""", nativeQuery = true)
    List<Object[]> findPairwiseBalancesByRoom(@Param("roomId") UUID roomId);

    @Modifying
    @Query(value = "REFRESH MATERIALIZED VIEW room_pair_balances_to_pay", nativeQuery = true)
    void refreshMaterializedView();
}
