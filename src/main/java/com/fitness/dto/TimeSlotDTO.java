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
public class TimeSlotDTO {
    private Long id;
    @NotNull(message = "Date is required")
    @Schema(description = "Date in format yyyy-MM-dd", example = "2025-05-21")
    private LocalDate date;
    @NotNull(message = "Start time is required")
    @Schema(description = "Start time in format HH:mm", example = "13:00")
    private LocalTime startTime;
    @NotNull(message = "End time is required")
    @Schema(description = "End time in format HH:mm", example = "14:30")
    private LocalTime endTime;

    private Boolean available= true;
    @NotNull(message = "Studio ID is required")
    @Schema(description = "Studio ID", example = "1")
    private Long studioId;
    @Schema(description = "Is the slot a trial (30 minutes)", example = "false")
    private boolean trial;

    public Long getStudioId() {
        return studioId;
    }

    public void setStudioId(Long studioId) {
        this.studioId = studioId;
    }
}
