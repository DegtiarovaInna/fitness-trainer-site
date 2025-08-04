package com.fitness.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentRequest {
    @NotNull
    Long bookingId;
    private String promoCode;
}
