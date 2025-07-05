package com.fitness.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOwnBookingRequest {
    @NotNull
    private Long timeSlotId;
}
