package com.bodimTikka.bodimTikka.repository;

import com.bodimTikka.bodimTikka.DTO.UserPaymentLogDTO;
import com.bodimTikka.bodimTikka.model.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    @Query("SELECT p FROM Payment p WHERE p.room.id = :roomId ORDER BY p.paymentTimestamp DESC")
    List<Payment> findLastPaymentsByRoomId(@Param("roomId") Long roomId, Pageable pageable);

    @Query("SELECT new com.bodimTikka.bodimTikka.DTO.UserPaymentLogDTO( " +
            "pr.fromUser.id, pr.toUser.id, pr.amount, p.paymentTimestamp, p.description) " +
            "FROM PaymentRecord pr " +
            "JOIN pr.payment p " +
            "WHERE p.room.id = :roomId " +
            "AND pr.fromUser.id = :userId1 " +
            "AND pr.toUser.id = :userId2 " +
            "ORDER BY p.paymentTimestamp DESC")
    List<UserPaymentLogDTO> findLastPaymentsByRoomIdAndUsers(
            @Param("roomId") Long roomId,
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            Pageable pageable
    );

    // native query for psql
    @Query(value = "SELECT p.payment_id, p.amount, pr.from_user_id, " +
            "p.payment_timestamp, p.description, p.is_repayment, " +
            "STRING_AGG(pr.to_user_id::TEXT, '/') AS to_users " +
            "FROM payment_record pr " +
            "JOIN payment p ON pr.payment_id = p.payment_id " +
            "WHERE p.room_id = :roomId " +
            "GROUP BY p.payment_id, p.amount, pr.from_user_id, p.payment_timestamp, p.description " +
            "ORDER BY p.payment_timestamp DESC " +
            "LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Object[]> findLastRoomPayments(@Param("roomId") Long roomId, @Param("limit") int limit, @Param("offset") int offset);
}
