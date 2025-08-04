package com.fitness.controllers;

import com.fitness.dto.CreatePaymentRequest;
import com.fitness.dto.PaymentDTO;
import com.fitness.dto.PaymentFilter;
import com.fitness.services.interfaces.PaymentService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;


    @RateLimiter(name="paymentRateLimiter")
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

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEV')")
    public Page<PaymentDTO> search(
            @ParameterObject PaymentFilter filter,
            @ParameterObject Pageable pageable
    ) {
        return paymentService.search(filter, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DEV')")
    public PaymentDTO get(@PathVariable Long id) {
        return paymentService.getPayment(id);
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN','DEV')")
    public void refund(
            @PathVariable Long id,
            @RequestParam long amountCents
    ) {
        paymentService.refund(id, amountCents);
    }
}
