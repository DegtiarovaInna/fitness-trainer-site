package com.fitness.services.interfaces;

import com.fitness.dto.TimeSlotCreateDTO;
import com.fitness.dto.TimeSlotDTO;
import com.fitness.dto.TimeSlotUpdateDTO;

import java.time.LocalDate;
import java.util.List;

public interface TimeSlotService {
//    boolean isTrainerAvailable(Long studioId, LocalDate date,
//                               java.time.LocalTime startTime,
//                               java.time.LocalTime endTime);
    TimeSlotDTO createTimeSlot(TimeSlotCreateDTO dto);
    TimeSlotDTO getTimeSlot(Long id);
    List<TimeSlotDTO> getAllTimeSlots();
    TimeSlotDTO updateTimeSlot(Long id, TimeSlotUpdateDTO dto);
    void deleteTimeSlot(Long id);
    List<TimeSlotDTO> getTimeSlotsByStudio(Long studioId);
    List<TimeSlotDTO> getTimeSlotsByStudioAndDateRange(
            Long studioId, LocalDate startDate, LocalDate endDate);
    List<TimeSlotDTO> getAvailableSlotsByStudio(
            Long studioId, LocalDate startDate, LocalDate endDate);
}
