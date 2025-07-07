package com.fitness.services.impl;


import com.fitness.dto.TimeSlotCreateDTO;
import com.fitness.dto.TimeSlotDTO;
import com.fitness.dto.TimeSlotUpdateDTO;
import com.fitness.exceptions.StudioNotFoundException;
import com.fitness.exceptions.TimeSlotInvalidTimeException;
import com.fitness.exceptions.TimeSlotNotFoundException;
import com.fitness.exceptions.TimeSlotOverlapException;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.mappers.TimeSlotMapper;
import com.fitness.models.Studio;
import com.fitness.models.TimeSlot;
import com.fitness.repositories.StudioRepository;
import com.fitness.repositories.TimeSlotRepository;
import com.fitness.services.interfaces.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fitness.services.interfaces.SecurityService;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements TimeSlotService {


    private final TimeSlotRepository timeSlotRepository;

    private final StudioRepository studioRepository;
    private final TimeSlotMapper timeSlotMapper;
    private final SecurityService    securityService;

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

    public List<TimeSlotDTO> getAvailableSlotsByStudio(Long studioId, LocalDate startDate, LocalDate endDate) {
        securityService.requireStudioOwnerOrAdminOrDev(studioId);
        return timeSlotRepository.findByStudioIdAndDateBetweenAndAvailableTrue(studioId, startDate, endDate).stream()
                .map(timeSlotMapper::timeSlotToTimeSlotDTO)
                .collect(Collectors.toList());
    }
}
