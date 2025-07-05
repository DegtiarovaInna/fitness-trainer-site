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
import com.fitness.services.interfaces.AuthService;
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
    private final AuthService authService;
   // private final AccessValidator accessValidator;
    private final SecurityService    securityService;
//    @Override
//    public boolean isTrainerAvailable(Long studioId, LocalDate date,
//                                      LocalTime startTime, LocalTime endTime) {
//        Studio studio = studioRepository.findById(studioId)
//                .orElseThrow(() -> new StudioNotFoundException(ErrorMessage.STUDIO_NOT_FOUND));
//        return isTrainerAvailable(studio, date, startTime, endTime, null);
//    }
//    private boolean isTrainerAvailable(Studio studio, LocalDate date, LocalTime startTime, LocalTime endTime, Long excludeSlotId) {
//        List<TimeSlot> sameDaySlots = timeSlotRepository.findByDate(date);
//
//        for (TimeSlot existingSlot : sameDaySlots) {
//            if (excludeSlotId != null && existingSlot.getId().equals(excludeSlotId)) {
//                // Пропускаем проверку самого себя при обновлении
//                continue;
//            }
//            Studio otherStudio = existingSlot.getStudio();
//            LocalTime existingStart = existingSlot.getStartTime();
//            LocalTime existingEnd = existingSlot.getEndTime();
//
//            if (!otherStudio.getId().equals(studio.getId())) {
//                // Межстудийная проверка с 1 часом буфера
//                if (!startTime.isAfter(existingEnd.plusHours(1)) && !endTime.isBefore(existingStart.minusHours(1))) {
//                    return false;
//                }
//            } else {
//                // В одной студии — проверка пересечения с разрешением слотов подряд
//                if (!(endTime.isBefore(existingStart) || !startTime.isBefore(existingEnd))) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
    @Override
    // Создание нового таймслота
    public TimeSlotDTO createTimeSlot(TimeSlotCreateDTO dto) {
        securityService.requireAdminOrDev();
        // 1) Базовая валидация времени
        if (dto.getStartTime().equals(dto.getEndTime())) {
            throw new TimeSlotInvalidTimeException(ErrorMessage.INVALID_TIME_RANGE);
        }
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new TimeSlotInvalidTimeException(ErrorMessage.INVALID_TIME_RANGE);
        }
        // 1) Проверяем дублирование:
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


        // Устанавливаем trial по логике
        boolean isTrial = java.time.Duration.between(dto.getStartTime(), dto.getEndTime()).toMinutes() == 30;
        timeSlot.setTrial(isTrial);

        TimeSlot saved = timeSlotRepository.save(timeSlot);
        return timeSlotMapper.timeSlotToTimeSlotDTO(saved);
    }
    // Получение таймслота по ID
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
    // 1) Валидация диапазона
    if (dto.getStartTime().equals(dto.getEndTime())
            || dto.getEndTime().isBefore(dto.getStartTime())) {
        throw new TimeSlotInvalidTimeException(ErrorMessage.INVALID_TIME_RANGE);
    }

    // 2) Загрузка существующего слота
    TimeSlot slot = timeSlotRepository.findById(id)
            .orElseThrow(() -> new TimeSlotNotFoundException(ErrorMessage.TIME_SLOT_NOT_FOUND));

    Long studioId = slot.getStudio().getId();  // ← берём из базы, а не из клиента

    // 3) Проверка пересечения в той же студии, исключая сам себя
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

    // 4) Применяем изменения
    slot.setDate(dto.getDate());
    slot.setStartTime(dto.getStartTime());
    slot.setEndTime(dto.getEndTime());
    // поле slot.getStudio() не трогаем
    slot.setTrial(
            Duration.between(dto.getStartTime(), dto.getEndTime()).toMinutes() == 30
    );

    // 5) Сохраняем и возвращаем
    TimeSlot updated = timeSlotRepository.save(slot);
    return timeSlotMapper.timeSlotToTimeSlotDTO(updated);
}
    @Override
    // Удаление таймслота по ID
    public void deleteTimeSlot(Long id) {
        securityService.requireAdminOrDev();
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new TimeSlotNotFoundException(ErrorMessage.TIME_SLOT_NOT_FOUND));

        // Удаляем таймслот
        timeSlotRepository.delete(timeSlot);
    }
    @Override
    public List<TimeSlotDTO> getTimeSlotsByStudio(Long studioId) {
        // ИЗМЕНЕНО: доступен только владелец студии (USER_PRO) или ADMIN/DEV
           securityService.requireStudioOwnerOrAdminOrDev(studioId);
//        if (authService.getCurrentUserRole() == Role.USER_PRO) {
//            if (!accessValidator.isCurrentStudio(studioId)) {
//                throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED);
//            }
//        }
        return timeSlotRepository.findByStudioId(studioId).stream()
                .map(timeSlotMapper::timeSlotToTimeSlotDTO)
                .collect(Collectors.toList());
    }
    @Override
//и доступные и нет для аналитики
    public List<TimeSlotDTO> getTimeSlotsByStudioAndDateRange(Long studioId, LocalDate startDate, LocalDate endDate) {
//        if (authService.getCurrentUserRole() == Role.USER_PRO) {
//            if (!accessValidator.isCurrentStudio(studioId)) {
//                throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED);
//            }
//        }
           securityService.requireStudioOwnerOrAdminOrDev(studioId);
        return timeSlotRepository.findByStudioIdAndDateBetween(studioId, startDate, endDate).stream()
                .map(timeSlotMapper::timeSlotToTimeSlotDTO)
                .collect(Collectors.toList());
    }
    @Override

    public List<TimeSlotDTO> getAvailableSlotsByStudio(Long studioId, LocalDate startDate, LocalDate endDate) {
//        if (authService.getCurrentUserRole() == Role.USER_PRO) {
//            if (!accessValidator.isCurrentStudio(studioId)) {
//                throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED);
//            }
//        }
        securityService.requireStudioOwnerOrAdminOrDev(studioId);
        return timeSlotRepository.findByStudioIdAndDateBetweenAndAvailableTrue(studioId, startDate, endDate).stream()
                .map(timeSlotMapper::timeSlotToTimeSlotDTO)
                .collect(Collectors.toList());
    }
}
