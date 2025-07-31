package com.fitness.repositories;

import com.fitness.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentIntentId(String piId);
    List<Payment> findByBookingId(Long bookingId);
}
