package com.fitness.services.impl;


import com.fitness.dto.TimeSlotCreateDTO;
import com.fitness.dto.TimeSlotDTO;
import com.fitness.dto.TimeSlotUpdateDTO;
import com.fitness.enums.BookingStatus;
import com.fitness.exceptions.*;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.mappers.TimeSlotMapper;
import com.fitness.models.Booking;
import com.fitness.models.Studio;
import com.fitness.models.TimeSlot;
import com.fitness.repositories.BookingRepository;
import com.fitness.repositories.StudioRepository;
import com.fitness.repositories.TimeSlotRepository;
import com.fitness.services.interfaces.TimeSlotService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fitness.services.interfaces.SecurityService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements TimeSlotService {

    private static final long INTER_STUDIO_BUFFER_HOURS = 1L;

    private final TimeSlotRepository timeSlotRepository;

    private final StudioRepository studioRepository;
    private final TimeSlotMapper timeSlotMapper;
    private final SecurityService securityService;
    private final BookingRepository bookingRepository;

    @Transactional
    @Override
    public TimeSlotDTO createTimeSlot(TimeSlotCreateDTO dto) {
        securityService.requireAdminOrDev();
        if (dto.getStartTime().equals(dto.getEndTime())) {
            throw new TimeSlotInvalidTimeException(ErrorMessage.INVALID_TIME_RANGE);
        }
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new TimeSlotInvalidTimeException(ErrorMessage.INVALID_TIME_RANGE);
        }
        if (timeSlotRepository.existsOverlapInStudio(
                dto.getStudioId(),
                dto.getDate(),
                dto.getStartTime(),
                dto.getEndTime(),
                null
        )) {
            throw new TimeSlotOverlapException(ErrorMessage.TIME_SLOT_OVERLAP);
        }
        Studio studio = studioRepository.findById(dto.getStudioId())
                .orElseThrow(() -> new StudioNotFoundException(ErrorMessage.STUDIO_NOT_FOUND));


        TimeSlot timeSlot = TimeSlot.builder()
                .date(dto.getDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .available(true)
                .trial(Duration.between(dto.getStartTime(), dto.getEndTime()).toMinutes() == 30)
                .studio(studio)
                .build();


        boolean isTrial = java.time.Duration.between(dto.getStartTime(), dto.getEndTime()).toMinutes() == 30;
        timeSlot.setTrial(isTrial);

        TimeSlot saved = timeSlotRepository.save(timeSlot);
        return timeSlotMapper.timeSlotToTimeSlotDTO(saved);
    }

    @Override
    public TimeSlotDTO getTimeSlot(Long id) {
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new TimeSlotNotFoundException(ErrorMessage.TIME_SLOT_NOT_FOUND));
        return timeSlotMapper.timeSlotToTimeSlotDTO(timeSlot);
    }

    @Override
    public List<TimeSlotDTO> getAllTimeSlots() {
        return timeSlotRepository.findAll().stream()
                .map(timeSlotMapper::timeSlotToTimeSlotDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public TimeSlotDTO updateTimeSlot(Long id, TimeSlotUpdateDTO dto) {
        securityService.requireAdminOrDev();
        if (dto.getStartTime().equals(dto.getEndTime())
                || dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new TimeSlotInvalidTimeException(ErrorMessage.INVALID_TIME_RANGE);
        }

        TimeSlot slot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new TimeSlotNotFoundException(ErrorMessage.TIME_SLOT_NOT_FOUND));

        Long studioId = slot.getStudio().getId();

        boolean overlap = timeSlotRepository.existsOverlapInStudio(
                studioId,
                dto.getDate(),
                dto.getStartTime(),
                dto.getEndTime(),
                id
        );
        if (overlap) {
            throw new TimeSlotOverlapException(ErrorMessage.TIME_SLOT_OVERLAP);
        }

        slot.setDate(dto.getDate());
        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        slot.setTrial(
                Duration.between(dto.getStartTime(), dto.getEndTime()).toMinutes() == 30
        );

        TimeSlot updated = timeSlotRepository.save(slot);
        return timeSlotMapper.timeSlotToTimeSlotDTO(updated);
    }

    @Override
    public void deleteTimeSlot(Long id) {
        securityService.requireAdminOrDev();
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new TimeSlotNotFoundException(ErrorMessage.TIME_SLOT_NOT_FOUND));
        if (bookingRepository.existsByTimeSlotIdAndStatusNot(id, BookingStatus.CANCELLED)) {
            throw new BookingCreationNotAllowedException(
                    "First cancel the reservation for this slot, then delete"
            );
        }

        timeSlotRepository.delete(timeSlot);
    }

    @Override
    public List<TimeSlotDTO> getTimeSlotsByStudio(Long studioId) {
        securityService.requireStudioOwnerOrAdminOrDev(studioId);
        return timeSlotRepository.findByStudioId(studioId).stream()
                .map(timeSlotMapper::timeSlotToTimeSlotDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getTimeSlotsByStudioAndDateRange(Long studioId, LocalDate startDate, LocalDate endDate) {
        securityService.requireStudioOwnerOrAdminOrDev(studioId);
        return timeSlotRepository.findByStudioIdAndDateBetween(studioId, startDate, endDate).stream()
                .map(timeSlotMapper::timeSlotToTimeSlotDTO)
                .collect(Collectors.toList());
    }


    @Override
    public List<TimeSlotDTO> getAvailableSlotsByStudio(Long studioId,
                                                       LocalDate startDate,
                                                       LocalDate endDate) {

        securityService.requireStudioOwnerOrAdminOrDev(studioId);

        List<TimeSlot> candidates = timeSlotRepository
                .findByStudioIdAndDateBetweenAndAvailableTrue(studioId, startDate, endDate);

        return candidates.stream()
                .filter(candidate -> {
                    LocalDate date = candidate.getDate();
                    LocalTime s1 = candidate.getStartTime();
                    LocalTime e1 = candidate.getEndTime();


                    List<Booking> bookings = bookingRepository
                            .findByTimeSlot_DateAndStatusNot(date, BookingStatus.CANCELLED);

                    for (Booking b : bookings) {
                        TimeSlot ex = b.getTimeSlot();
                        LocalTime s2 = ex.getStartTime();
                        LocalTime e2 = ex.getEndTime();

                        boolean sameStudio = ex.getStudio().getId().equals(studioId);

                        if (sameStudio) {
                            boolean overlaps = s1.isBefore(e2) && e1.isAfter(s2);
                            if (overlaps) {
                                return false;
                            }


                        } else {
                            if (s1.isBefore(e2.plusHours(INTER_STUDIO_BUFFER_HOURS)) &&
                                    e1.isAfter(s2.minusHours(INTER_STUDIO_BUFFER_HOURS))) {
                                return false;
                            }
                        }
                    }
                    return true;
                })
                .map(timeSlotMapper::timeSlotToTimeSlotDTO)
                .collect(Collectors.toList());
    }
}
