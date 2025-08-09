package com.fitness.controllers;

import com.fitness.dto.CreatePaymentRequest;
import com.fitness.dto.PaymentDTO;
import com.fitness.dto.PaymentFilter;
import com.fitness.services.interfaces.PaymentService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "Payment", description = "Payments and Stripe integration")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @Operation(summary = "Create payment intent",
            description = "Returns client secret and payment meta.")
    @ApiResponse(responseCode = "200", description = "Created",
            content = @Content(schema = @Schema(implementation = PaymentDTO.class)))
    @RateLimiter(name = "paymentRateLimiter")
    @PostMapping
    public ResponseEntity<PaymentDTO> create(@Valid @RequestBody CreatePaymentRequest req) {
        return ResponseEntity.ok(
                paymentService.createPaymentIntent(req.getBookingId(), req.getPromoCode())
        );
    }

    @Operation(summary = "Stripe webhook", description = "Receives Stripe events.")
    @ApiResponse(responseCode = "200", description = "Accepted (processed)")
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sig
    ) {
        paymentService.handleWebhook(payload, sig);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Search payments (admin)")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = org.springframework.data.domain.Page.class)))
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEV')")
    public Page<PaymentDTO> search(
            @ParameterObject PaymentFilter filter,
            @ParameterObject Pageable pageable
    ) {
        return paymentService.search(filter, pageable);
    }

    @Operation(summary = "Get payment by id (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(schema = @Schema(implementation = PaymentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DEV')")
    public PaymentDTO get(@PathVariable Long id) {
        return paymentService.getPayment(id);
    }

    @Operation(summary = "Refund payment (admin)")
    @ApiResponse(responseCode = "200", description = "Refunded")
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN','DEV')")
    public void refund(
            @PathVariable Long id,
            @RequestParam long amountCents
    ) {
        paymentService.refund(id, amountCents);
    }
}
