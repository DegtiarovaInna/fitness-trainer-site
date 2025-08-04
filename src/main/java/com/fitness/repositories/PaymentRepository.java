package com.fitness.repositories;

import com.fitness.enums.PaymentStatus;
import com.fitness.models.Payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentIntentId(String piId);
    //List<Payment> findByBookingId(Long bookingId);
    @Query("""
        SELECT p FROM Payment p
        WHERE (:bookingId IS NULL OR p.bookingId = :bookingId)
          AND (:userId    IS NULL OR EXISTS (
                    SELECT 1 FROM Booking b
                    WHERE b.id = p.bookingId
                      AND b.user.id = :userId))
          AND (:status    IS NULL OR p.status = :status)
          AND (:fromDate  IS NULL OR p.createdAt >= :fromDate)
          AND (:toDate    IS NULL OR p.createdAt <= :toDate)
    """)
    Page<Payment> search(
            @Param("bookingId") Long bookingId,
            @Param("userId")    Long userId,
            @Param("status") PaymentStatus status,
            @Param("fromDate") LocalDateTime from,
            @Param("toDate")    LocalDateTime to,
            Pageable pageable
    );
}
