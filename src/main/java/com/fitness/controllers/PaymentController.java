package com.fitness.controllers;

import com.fitness.dto.CreatePaymentRequest;
import com.fitness.dto.PaymentDTO;
import com.stripe.model.PaymentIntent;
import com.fitness.services.interfaces.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentDTO> create(@Valid @RequestBody CreatePaymentRequest req) {
        return ResponseEntity.ok(
                paymentService.createPaymentIntent(req.getBookingId(), req.getPromoCode())
        );
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sig
    ) {
        paymentService.handleWebhook(payload, sig);
        return ResponseEntity.ok().build();
    }
}
