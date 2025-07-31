package com.fitness.dto;

import com.fitness.enums.PaymentStatus;
import lombok.Data;

@Data
public class PaymentDTO {
    private Long id;
    private Long bookingId;
    private long amount;
    private String currency;
    private PaymentStatus status;
    private String clientSecret;
    private String promoCode;
}
