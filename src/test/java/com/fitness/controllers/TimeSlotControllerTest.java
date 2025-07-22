package com.fitness.controllers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.TimeSlotCreateDTO;
import com.fitness.dto.TimeSlotDTO;
import com.fitness.dto.TimeSlotUpdateDTO;
import com.fitness.exceptions.TimeSlotNotFoundException;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.services.interfaces.TimeSlotService;
import com.fitness.config.security.JwtService;
import com.fitness.config.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TimeSlotController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TimeSlotControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private TimeSlotService timeSlotService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("POST /api/timeslots — successful slot creation")
    void createTimeSlot_success() throws Exception {
        var req = new TimeSlotCreateDTO(
                LocalDate.parse("2025-07-15"),
                LocalTime.parse("09:00"),
                LocalTime.parse("10:00"),
                1L
        );

        var dto = new TimeSlotDTO(
                1L,
                LocalDate.parse("2025-07-15"),
                LocalTime.parse("09:00"),
                LocalTime.parse("10:00"),
                true,
                1L,
                false
        );

        when(timeSlotService.createTimeSlot(req)).thenReturn(dto);

        mvc.perform(post("/api/timeslots")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.studioId").value(1))
                .andExpect(jsonPath("$.date").value("2025-07-15"))
                .andExpect(jsonPath("$.startTime").value("09:00:00"))
                .andExpect(jsonPath("$.endTime").value("10:00:00"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.trial").value(false));
    }

    @Test
    @DisplayName("GET /api/timeslots/{id} — successful get of slot")
    void getTimeSlot_success() throws Exception {
        var dto = new TimeSlotDTO(
                2L,
                LocalDate.parse("2025-07-16"),
                LocalTime.parse("11:00"),
                LocalTime.parse("12:00"),
                true,
                2L,
                true
        );

        when(timeSlotService.getTimeSlot(2L)).thenReturn(dto);

        mvc.perform(get("/api/timeslots/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.trial").value(true));
    }

    @Test
    @DisplayName("GET /api/timeslots/{id} — slot not found")
    void getTimeSlot_notFound() throws Exception {
        when(timeSlotService.getTimeSlot(99L))
                .thenThrow(new TimeSlotNotFoundException(ErrorMessage.TIME_SLOT_NOT_FOUND));

        mvc.perform(get("/api/timeslots/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ErrorMessage.TIME_SLOT_NOT_FOUND));
    }

    @Test
    @DisplayName("GET /api/timeslots — successful get of all slots")
    void getAllTimeSlots_success() throws Exception {
        var dto1 = new TimeSlotDTO(3L, LocalDate.now(), LocalTime.NOON, LocalTime.NOON.plusHours(1), true, 3L, false);
        var dto2 = new TimeSlotDTO(4L, LocalDate.now(), LocalTime.NOON, LocalTime.NOON.plusHours(1), false, 3L, false);

        when(timeSlotService.getAllTimeSlots()).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/api/timeslots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[1].available").value(false));
    }

    @Test
    @DisplayName("PUT /api/timeslots/{id} —successful slot update")
    void updateTimeSlot_success() throws Exception {
        var req = new TimeSlotUpdateDTO(
                LocalDate.parse("2025-07-20"),
                LocalTime.parse("14:00"),
                LocalTime.parse("15:00")
        );
        var dto = new TimeSlotDTO(
                5L,
                LocalDate.parse("2025-07-20"),
                LocalTime.parse("14:00"),
                LocalTime.parse("15:00"),
                true,
                4L,
                false
        );

        when(timeSlotService.updateTimeSlot(5L, req)).thenReturn(dto);

        mvc.perform(put("/api/timeslots/5")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.date").value("2025-07-20"));
    }

    @Test
    @DisplayName("PUT /api/timeslots/{id} — slot not found during update")
    void updateTimeSlot_notFound() throws Exception {
        var req = new TimeSlotUpdateDTO(
                LocalDate.parse("2025-07-21"),
                LocalTime.parse("16:00"),
                LocalTime.parse("17:00")
        );

        doThrow(new TimeSlotNotFoundException(ErrorMessage.TIME_SLOT_NOT_FOUND))
                .when(timeSlotService).updateTimeSlot(eq(6L), any());

        mvc.perform(put("/api/timeslots/6")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ErrorMessage.TIME_SLOT_NOT_FOUND));
    }

    @Test
    @DisplayName("DELETE /api/timeslots/{id} — successful slot delete")
    void deleteTimeSlot_success() throws Exception {
        doNothing().when(timeSlotService).deleteTimeSlot(7L);

        mvc.perform(delete("/api/timeslots/7"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/timeslots/{id} — slot not found when deleting")
    void deleteTimeSlot_notFound() throws Exception {
        doThrow(new TimeSlotNotFoundException(ErrorMessage.TIME_SLOT_NOT_FOUND))
                .when(timeSlotService).deleteTimeSlot(8L);

        mvc.perform(delete("/api/timeslots/8"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ErrorMessage.TIME_SLOT_NOT_FOUND));
    }

    @Test
    @DisplayName("GET /api/timeslots/studio/{studioId} — successful receipt of slots by studio")
    void getTimeSlotsByStudio_success() throws Exception {
        var dto = new TimeSlotDTO(9L, LocalDate.now(), LocalTime.NOON, LocalTime.NOON.plusHours(1), true, 10L, false);

        when(timeSlotService.getTimeSlotsByStudio(10L)).thenReturn(List.of(dto));

        mvc.perform(get("/api/timeslots/studio/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].studioId").value(10));
    }

    @Test
    @DisplayName("GET /api/timeslots/studio/{studioId}/available — successful get of available slots")
    void getAvailableSlots_success() throws Exception {
        var dto = new TimeSlotDTO(11L, LocalDate.parse("2025-07-01"), LocalTime.parse("08:00"), LocalTime.parse("09:00"), true, 12L, false);

        when(timeSlotService.getAvailableSlotsByStudio(
                eq(12L),
                eq(LocalDate.parse("2025-07-01")),
                eq(LocalDate.parse("2025-07-31"))
        )).thenReturn(List.of(dto));

        mvc.perform(get("/api/timeslots/studio/12/available")
                        .param("start", "2025-07-01")
                        .param("end", "2025-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11));
    }

    @Test
    @DisplayName("GET /api/timeslots/studio/{studioId}/dates — successful get of slots by date range")
    void getTimeSlotsByStudioAndDateRange_success() throws Exception {
        var dto = new TimeSlotDTO(13L, LocalDate.parse("2025-07-05"), LocalTime.parse("10:00"), LocalTime.parse("11:00"), false, 14L, false);

        when(timeSlotService.getTimeSlotsByStudioAndDateRange(
                eq(14L),
                eq(LocalDate.parse("2025-07-01")),
                eq(LocalDate.parse("2025-07-31"))
        )).thenReturn(List.of(dto));

        mvc.perform(get("/api/timeslots/studio/14/dates")
                        .param("start", "2025-07-01")
                        .param("end", "2025-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(13))
                .andExpect(jsonPath("$[0].available").value(false));
    }
}
