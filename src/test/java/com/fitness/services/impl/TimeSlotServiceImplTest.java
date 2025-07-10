package com.fitness.services.impl;
import com.fitness.dto.TimeSlotCreateDTO;
import com.fitness.dto.TimeSlotDTO;
import com.fitness.dto.TimeSlotUpdateDTO;
import com.fitness.exceptions.StudioNotFoundException;
import com.fitness.exceptions.TimeSlotInvalidTimeException;
import com.fitness.exceptions.TimeSlotNotFoundException;
import com.fitness.exceptions.TimeSlotOverlapException;
import com.fitness.models.Studio;
import com.fitness.models.TimeSlot;
import com.fitness.repositories.TimeSlotRepository;
import com.fitness.repositories.StudioRepository;
import com.fitness.mappers.TimeSlotMapper;
import com.fitness.services.interfaces.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class TimeSlotServiceImplTest {
    private TimeSlotRepository slotRepo;
    private StudioRepository studioRepo;
    private TimeSlotMapper mapper;
    private SecurityService securityService;
    private TimeSlotServiceImpl service;

    @BeforeEach
    void setUp() {
        slotRepo = mock(TimeSlotRepository.class);
        studioRepo = mock(StudioRepository.class);
        mapper = mock(TimeSlotMapper.class);
        securityService = mock(SecurityService.class);
        doNothing().when(securityService).requireAdminOrDev();

        service = new TimeSlotServiceImpl(slotRepo, studioRepo, mapper, securityService);
    }

    //createTimeSlot

    @Test
    void create_startEqualsEnd_invalidTime() {
        var dto = new TimeSlotCreateDTO(LocalDate.now(), LocalTime.of(10,0), LocalTime.of(10,0), 1L);
        var ex = assertThrows(TimeSlotInvalidTimeException.class, () -> service.createTimeSlot(dto));
        assertEquals("The end time must be later than the start time.", ex.getMessage());
        verify(slotRepo, never()).existsOverlapInStudio(any(), any(), any(), any(), isNull());
    }

    @Test
    void create_endBeforeStart_invalidTime() {
        var dto = new TimeSlotCreateDTO(LocalDate.now(), LocalTime.of(14,0), LocalTime.of(13,0), 1L);
        assertThrows(TimeSlotInvalidTimeException.class, () -> service.createTimeSlot(dto));
        verify(slotRepo, never()).existsOverlapInStudio(any(), any(), any(), any(), isNull());
    }

    @Test
    void create_whenOverlap_throwsOverlap() {
        var dto = new TimeSlotCreateDTO(LocalDate.now(), LocalTime.of(9,0), LocalTime.of(10,0), 1L);
        when(slotRepo.existsOverlapInStudio(1L, dto.getDate(), dto.getStartTime(), dto.getEndTime(), null)).thenReturn(true);
        assertThrows(TimeSlotOverlapException.class, () -> service.createTimeSlot(dto));
        verify(studioRepo, never()).findById(any());
    }

    @Test
    void create_whenStudioMissing_throwsNotFound() {
        var dto = new TimeSlotCreateDTO(LocalDate.now(), LocalTime.of(9,0), LocalTime.of(10,0), 2L);
        when(slotRepo.existsOverlapInStudio(any(), any(), any(), any(), isNull())).thenReturn(false);
        when(studioRepo.findById(2L)).thenReturn(Optional.empty());
        assertThrows(StudioNotFoundException.class, () -> service.createTimeSlot(dto));
    }

    @Test
    void create_successful() {
        var dto = new TimeSlotCreateDTO(LocalDate.of(2025,7,10), LocalTime.of(12,0), LocalTime.of(13,30), 3L);
        when(slotRepo.existsOverlapInStudio(any(), any(), any(), any(), isNull())).thenReturn(false);
        var studio = new Studio(); studio.setId(3L);
        when(studioRepo.findById(3L)).thenReturn(Optional.of(studio));

        var entity = TimeSlot.builder()
                .id(10L)
                .date(dto.getDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .available(true)
                .trial(Duration.between(dto.getStartTime(), dto.getEndTime()).toMinutes() == 30)
                .studio(studio)
                .build();
        when(slotRepo.save(any(TimeSlot.class))).thenReturn(entity);

        var expected = new TimeSlotDTO(); expected.setId(10L);
        when(mapper.timeSlotToTimeSlotDTO(entity)).thenReturn(expected);

        var result = service.createTimeSlot(dto);

        verify(securityService).requireAdminOrDev();
        verify(slotRepo).existsOverlapInStudio(3L, dto.getDate(), dto.getStartTime(), dto.getEndTime(), null);
        verify(studioRepo).findById(3L);
        verify(slotRepo).save(any());
        verify(mapper).timeSlotToTimeSlotDTO(entity);
        assertSame(expected, result);
    }

    //getTimeSlot

    @Test
    void get_existing_returnsDto() {
        var ts = new TimeSlot(); ts.setId(5L);
        when(slotRepo.findById(5L)).thenReturn(Optional.of(ts));
        var dto = new TimeSlotDTO(); dto.setId(5L);
        when(mapper.timeSlotToTimeSlotDTO(ts)).thenReturn(dto);
        assertSame(dto, service.getTimeSlot(5L));
    }

    @Test
    void get_missing_throwsNotFound() {
        when(slotRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(TimeSlotNotFoundException.class, () -> service.getTimeSlot(99L));
    }

    //getAllTimeSlots

    @Test
    void getAll_mapsAll() {
        var e1 = new TimeSlot(); e1.setId(1L);
        var e2 = new TimeSlot(); e2.setId(2L);
        when(slotRepo.findAll()).thenReturn(List.of(e1, e2));
        var d1 = new TimeSlotDTO(); d1.setId(1L);
        var d2 = new TimeSlotDTO(); d2.setId(2L);
        when(mapper.timeSlotToTimeSlotDTO(e1)).thenReturn(d1);
        when(mapper.timeSlotToTimeSlotDTO(e2)).thenReturn(d2);
        var list = service.getAllTimeSlots();
        assertEquals(List.of(d1, d2), list);
    }

    //updateTimeSlot

    @Test
    void update_invalidTime_throwsInvalidTime() {
        var dto = new TimeSlotUpdateDTO(LocalDate.now(), LocalTime.of(5,0), LocalTime.of(5,0));
        assertThrows(TimeSlotInvalidTimeException.class, () -> service.updateTimeSlot(1L, dto));
    }

    @Test
    void update_missing_throwsNotFound() {
        var dto = new TimeSlotUpdateDTO(LocalDate.now(), LocalTime.of(6,0), LocalTime.of(7,0));
        when(slotRepo.findById(7L)).thenReturn(Optional.empty());
        assertThrows(TimeSlotNotFoundException.class, () -> service.updateTimeSlot(7L, dto));
    }

    @Test
    void update_overlap_throwsOverlap() {
        var dto = new TimeSlotUpdateDTO(LocalDate.now(), LocalTime.of(8,0), LocalTime.of(9,0));
        var existing = TimeSlot.builder().id(8L).studio(new Studio()).build();
        when(slotRepo.findById(8L)).thenReturn(Optional.of(existing));
        when(slotRepo.existsOverlapInStudio( existing.getStudio().getId(), dto.getDate(), dto.getStartTime(), dto.getEndTime(), 8L))
                .thenReturn(true);
        assertThrows(TimeSlotOverlapException.class, () -> service.updateTimeSlot(8L, dto));
    }

    @Test
    void update_successful_returnsDto() {
        var dto = new TimeSlotUpdateDTO(LocalDate.of(2025,8,1), LocalTime.of(10,0), LocalTime.of(11,0));
        var studio = new Studio(); studio.setId(9L);
        var existing = TimeSlot.builder().id(9L).studio(studio).build();
        when(slotRepo.findById(9L)).thenReturn(Optional.of(existing));
        when(slotRepo.existsOverlapInStudio(9L, dto.getDate(), dto.getStartTime(), dto.getEndTime(), 9L))
                .thenReturn(false);
        var updated = TimeSlot.builder().id(9L).studio(studio).build();
        when(slotRepo.save(existing)).thenReturn(updated);
        var dtoResult = new TimeSlotDTO(); dtoResult.setId(9L);
        when(mapper.timeSlotToTimeSlotDTO(updated)).thenReturn(dtoResult);

        assertSame(dtoResult, service.updateTimeSlot(9L, dto));
        verify(securityService).requireAdminOrDev();
    }

    //deleteTimeSlot

    @Test
    void delete_missing_throwsNotFound() {
        when(slotRepo.findById(100L)).thenReturn(Optional.empty());
        assertThrows(TimeSlotNotFoundException.class, () -> service.deleteTimeSlot(100L));
    }

    @Test
    void delete_existing_deletes() {
        var slot = new TimeSlot(); slot.setId(11L);
        when(slotRepo.findById(11L)).thenReturn(Optional.of(slot));
        doNothing().when(slotRepo).delete(slot);
        service.deleteTimeSlot(11L);
        verify(securityService).requireAdminOrDev();
        verify(slotRepo).delete(slot);
    }

    //query methods

    @Test
    void getByStudio_mapsResults() {
        var s1 = new TimeSlot(); s1.setId(21L);
        when(slotRepo.findByStudioId(5L)).thenReturn(List.of(s1));
        var dto = new TimeSlotDTO(); dto.setId(21L);
        when(mapper.timeSlotToTimeSlotDTO(s1)).thenReturn(dto);
        assertEquals(List.of(dto), service.getTimeSlotsByStudio(5L));
        verify(securityService).requireStudioOwnerOrAdminOrDev(5L);
    }

    @Test
    void getByDateRange_mapsResults() {
        var s2 = new TimeSlot(); s2.setId(22L);
        when(slotRepo.findByStudioIdAndDateBetween(6L, LocalDate.of(2025,1,1), LocalDate.of(2025,1,31)))
                .thenReturn(List.of(s2));
        var dto2 = new TimeSlotDTO(); dto2.setId(22L);
        when(mapper.timeSlotToTimeSlotDTO(s2)).thenReturn(dto2);
        assertEquals(List.of(dto2), service.getTimeSlotsByStudioAndDateRange(6L, LocalDate.of(2025,1,1), LocalDate.of(2025,1,31)));
        verify(securityService).requireStudioOwnerOrAdminOrDev(6L);
    }

    @Test
    void getAvailable_mapsResults() {
        var s3 = new TimeSlot(); s3.setId(23L);
        when(slotRepo.findByStudioIdAndDateBetweenAndAvailableTrue(7L, LocalDate.of(2025,2,1), LocalDate.of(2025,2,28)))
                .thenReturn(List.of(s3));
        var dto3 = new TimeSlotDTO(); dto3.setId(23L);
        when(mapper.timeSlotToTimeSlotDTO(s3)).thenReturn(dto3);
        assertEquals(List.of(dto3), service.getAvailableSlotsByStudio(7L, LocalDate.of(2025,2,1), LocalDate.of(2025,2,28)));
        verify(securityService).requireStudioOwnerOrAdminOrDev(7L);
    }
}
