package com.fitness.services.interfaces;

import com.fitness.dto.PaymentDTO;

public interface PaymentService {
    PaymentDTO createPaymentIntent(Long bookingId, String promoCode);
    void handleWebhook(String payload, String sigHeader);
}
