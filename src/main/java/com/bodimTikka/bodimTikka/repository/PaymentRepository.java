package com.bodimTikka.bodimTikka.repository;

import com.bodimTikka.bodimTikka.model.Payment;
import com.bodimTikka.bodimTikka.model.Room;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    @Query("SELECT p FROM Payment p WHERE p.room.id = :roomId ORDER BY p.paymentTimestamp DESC")
    List<Payment> findLastPaymentsByRoomId(@Param("roomId") Long roomId, Pageable pageable);
}
