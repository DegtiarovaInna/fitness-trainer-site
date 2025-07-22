package com.fitness.controllers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.dto.BookingDTO;
import com.fitness.dto.CreateBookingRequest;
import com.fitness.dto.CreateOwnBookingRequest;
import com.fitness.dto.UpdateBookingRequest;
import com.fitness.enums.BookingStatus;
import com.fitness.exceptions.BookingAlreadyCancelledException;
import com.fitness.exceptions.BookingNotFoundException;
import com.fitness.exceptions.BookingCreationNotAllowedException;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.services.interfaces.BookingService;
import com.fitness.config.security.JwtService;
import com.fitness.config.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BookingControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("POST /api/bookings/me — successful own booking")
    void createOwnBooking_success() throws Exception {
        var req = new CreateOwnBookingRequest();
        req.setTimeSlotId(10L);

        var dto = new BookingDTO();
        dto.setId(1L);
        dto.setTimeSlotId(10L);
        dto.setStatus(BookingStatus.PENDING);

        when(bookingService.createBookingForCurrentUser(10L)).thenReturn(dto);

        mvc.perform(post("/api/bookings/me")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.timeSlotId").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/bookings/me — creation not allowed → 403 FORBIDDEN")
    void createOwnBooking_notAllowed() throws Exception {
        var req = new CreateOwnBookingRequest();
        req.setTimeSlotId(10L);

        doThrow(new BookingCreationNotAllowedException(ErrorMessage.BOOKING_CREATE_FOR_ANOTHER_USER))
                .when(bookingService).createBookingForCurrentUser(10L);

        mvc.perform(post("/api/bookings/me")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message")
                        .value(ErrorMessage.BOOKING_CREATE_FOR_ANOTHER_USER));
    }

    @Test
    @DisplayName("POST /api/bookings — successful user booking")
    void createBookingForUser_success() throws Exception {
        var req = new CreateBookingRequest();
        req.setUserId(2L);
        req.setTimeSlotId(20L);

        var dto = new BookingDTO();
        dto.setId(2L);
        dto.setUserId(2L);
        dto.setTimeSlotId(20L);
        dto.setStatus(BookingStatus.CONFIRMED);

        when(bookingService.createBooking(req)).thenReturn(dto);

        mvc.perform(post("/api/bookings")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.timeSlotId").value(20))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("POST /api/bookings — creation not allowed → 403 FORBIDDEN")
    void createBookingForUser_notAllowed() throws Exception {
        var req = new CreateBookingRequest();
        req.setUserId(2L);
        req.setTimeSlotId(20L);

        doThrow(new BookingCreationNotAllowedException(ErrorMessage.BOOKING_CREATE_FOR_ANOTHER_USER))
                .when(bookingService).createBooking(req);

        mvc.perform(post("/api/bookings")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message")
                        .value(ErrorMessage.BOOKING_CREATE_FOR_ANOTHER_USER));
    }

    @Test
    @DisplayName("GET /api/bookings/{id} — success")
    void getBooking_success() throws Exception {
        var dto = new BookingDTO();
        dto.setId(3L);
        dto.setStatus(BookingStatus.CONFIRMED);

        when(bookingService.getBooking(3L)).thenReturn(dto);

        mvc.perform(get("/api/bookings/3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("GET /api/bookings/{id} — not found → 404 NOT_FOUND")
    void getBooking_notFound() throws Exception {
        when(bookingService.getBooking(99L))
                .thenThrow(new BookingNotFoundException(ErrorMessage.BOOKING_NOT_FOUND));

        mvc.perform(get("/api/bookings/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value(ErrorMessage.BOOKING_NOT_FOUND));
    }

    @Test
    @DisplayName("GET /api/bookings ")
    void getAllBookings_success() throws Exception {
        var dto1 = new BookingDTO(); dto1.setId(4L);
        var dto2 = new BookingDTO(); dto2.setId(5L);

        when(bookingService.getAllBookings()).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[1].id").value(5));
    }

    @Test
    @DisplayName("PUT /api/bookings/{id}/cancel — success")
    void cancelBooking_success() throws Exception {
        var dto = new BookingDTO();
        dto.setId(6L);
        dto.setStatus(BookingStatus.CANCELLED);

        when(bookingService.cancelBooking(6L)).thenReturn(dto);

        mvc.perform(put("/api/bookings/6/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PUT /api/bookings/{id}/cancel — not found → 404 NOT_FOUND")
    void cancelBooking_notFound() throws Exception {
        doThrow(new BookingNotFoundException(ErrorMessage.BOOKING_NOT_FOUND))
                .when(bookingService).cancelBooking(7L);

        mvc.perform(put("/api/bookings/7/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value(ErrorMessage.BOOKING_NOT_FOUND));
    }

    @Test
    @DisplayName("PUT /api/bookings/{id}/cancel — already cancelled → 400 BUSINESS_ERROR")
    void cancelBooking_alreadyCancelled() throws Exception {
        doThrow(new BookingAlreadyCancelledException(ErrorMessage.BOOKING_ALREADY_CANCELLED))
                .when(bookingService).cancelBooking(8L);

        mvc.perform(put("/api/bookings/8/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value(ErrorMessage.BOOKING_ALREADY_CANCELLED));
    }

    @Test
    @DisplayName("PUT /api/bookings/{id} — success")
    void updateBooking_success() throws Exception {
        var req = new UpdateBookingRequest();
        req.setTimeSlotId(30L);
        req.setStatus(BookingStatus.CONFIRMED);

        var dto = new BookingDTO();
        dto.setId(9L);
        dto.setTimeSlotId(30L);
        dto.setStatus(BookingStatus.CONFIRMED);

        when(bookingService.updateBooking(eq(9L), eq(req))).thenReturn(dto);

        mvc.perform(put("/api/bookings/9")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.timeSlotId").value(30))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PUT /api/bookings/{id} — not found → 404 NOT_FOUND")
    void updateBooking_notFound() throws Exception {
        var req = new UpdateBookingRequest();
        req.setTimeSlotId(30L);
        req.setStatus(BookingStatus.PENDING);

        doThrow(new BookingNotFoundException(ErrorMessage.BOOKING_NOT_FOUND))
                .when(bookingService).updateBooking(eq(10L), any());

        mvc.perform(put("/api/bookings/10")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value(ErrorMessage.BOOKING_NOT_FOUND));
    }


    @Test
    @DisplayName("GET /api/bookings/search — successful search for bookings")
    void searchBookings_success() throws Exception {
        var dto = new BookingDTO(); dto.setId(11L);
        when(bookingService.searchBookings(
                eq(1L), eq(5L), eq(BookingStatus.PENDING),
                eq(LocalDate.parse("2025-07-01")), eq(LocalDate.parse("2025-07-31"))
        )).thenReturn(List.of(dto));

        mvc.perform(get("/api/bookings/search")
                        .param("userId", "1")
                        .param("studioId", "5")
                        .param("status", "PENDING")
                        .param("start", "2025-07-01")
                        .param("end",   "2025-07-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(11));
    }

    @Test
    @DisplayName("GET /api/bookings/me/upcoming — successful issuance of upcoming bookings")
    void getMyUpcoming_success() throws Exception {
        var dto = new BookingDTO(); dto.setId(12L);
        when(bookingService.getMyUpcoming()).thenReturn(List.of(dto));

        mvc.perform(get("/api/bookings/me/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(12));
    }

    @Test
    @DisplayName("GET /api/bookings/me/history — successful issuance of booking history")
    void getMyHistory_success() throws Exception {
        var dto = new BookingDTO(); dto.setId(13L);
        when(bookingService.getMyHistory()).thenReturn(List.of(dto));

        mvc.perform(get("/api/bookings/me/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(13));
    }
}
