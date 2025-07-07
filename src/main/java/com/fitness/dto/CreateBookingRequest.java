package com.fitness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBookingRequest {
    @NotNull(message = "User ID is required")
    @Schema(description = "User ID who is booking", example = "2")
    private Long userId;
    @NotNull(message = "Timeslot ID is required")
    @Schema(description = "ID of the selected timeslot", example = "10")
    private Long timeSlotId;
}
