package com.fitness.services.interfaces;

import com.fitness.dto.PaymentDTO;
import com.fitness.dto.PaymentFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentDTO createPaymentIntent(Long bookingId, String promoCode);
    void handleWebhook(String payload, String sigHeader);
    Page<PaymentDTO> search(PaymentFilter filter, Pageable pageable);
    PaymentDTO       getPayment(Long id);
    void             refund(Long id, long amountCents);
}
