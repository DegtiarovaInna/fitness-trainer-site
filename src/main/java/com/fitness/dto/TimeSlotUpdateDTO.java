package com.fitness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotUpdateDTO {
    @NotNull(message = "Дата обязательна")
    @Schema(description = "Дата в формате yyyy-MM-dd", example = "2025-05-21")
    private LocalDate date;

    @NotNull(message = "Время начала обязательно")
    @Schema(description = "Время начала в формате HH:mm", example = "13:00")
    private LocalTime startTime;

    @NotNull(message = "Время окончания обязательно")
    @Schema(description = "Время окончания в формате HH:mm", example = "14:30")
    private LocalTime endTime;
}
