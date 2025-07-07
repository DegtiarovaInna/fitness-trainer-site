package com.fitness.dto;

import com.fitness.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UpdateBookingRequest {
    private Long timeSlotId;
    @Schema(
            description = "New booking status (PENDING, CONFIRMED, CANCELLED)",
            example = "CONFIRMED"
    )
    private BookingStatus status;
}
