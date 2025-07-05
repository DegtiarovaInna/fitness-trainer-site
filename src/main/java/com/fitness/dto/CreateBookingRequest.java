package com.fitness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBookingRequest {
    @NotNull(message = "ID пользователя обязателен")
    @Schema(description = "ID пользователя, который бронирует", example = "2")
    private Long userId;
    @NotNull(message = "ID таймслота обязателен")
    @Schema(description = "ID выбранного таймслота", example = "10")
    private Long timeSlotId;
}
