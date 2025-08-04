package com.fitness.dto;

import com.fitness.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentFilter {
    private Long bookingId;
    private Long userId;
    private PaymentStatus status;
    private LocalDate from;
    private LocalDate to;
}
