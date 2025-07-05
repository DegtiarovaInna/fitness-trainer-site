package com.fitness.dto;

import com.fitness.enums.BookingStatus;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class BookingDTO {
    private Long id;
    private Long userId;
    private Long timeSlotId;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
