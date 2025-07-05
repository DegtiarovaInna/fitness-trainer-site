package com.fitness.controllers;

import com.fitness.dto.BookingDTO;
import com.fitness.dto.CreateBookingRequest;
import com.fitness.dto.CreateOwnBookingRequest;
import com.fitness.dto.UpdateBookingRequest;
import com.fitness.enums.BookingStatus;
import com.fitness.services.interfaces.BookingService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Booking", description = "Управление бронированиями")
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    @PostMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingDTO> createOwnBooking(
            @Valid @RequestBody CreateOwnBookingRequest req) {
        BookingDTO dto = bookingService.createBookingForCurrentUser(req.getTimeSlotId());
        return ResponseEntity.ok(dto);
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEV')")
    public ResponseEntity<BookingDTO> createBookingForUser(
            @Valid @RequestBody CreateBookingRequest req) {
        BookingDTO dto = bookingService.createBooking(req);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<BookingDTO> getBooking(@PathVariable Long id) {
        BookingDTO bookingDTO = bookingService.getBooking(id);
        return ResponseEntity.ok(bookingDTO);
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEV')")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable Long id) {
        BookingDTO bookingDTO = bookingService.cancelBooking(id);
        return ResponseEntity.ok(bookingDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<BookingDTO> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingRequest req) {
        BookingDTO dto = bookingService.updateBooking(id, req);
        return ResponseEntity.ok(dto);
    }
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER','USER_PRO','ADMIN','DEV')")
    public ResponseEntity<List<BookingDTO>> searchBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long studioId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @Parameter(description="Начало периода, формат yyyy-MM-dd", example="2025-07-01") LocalDate start,
            @RequestParam(required = false) @Parameter(description="Конец периода, формат yyyy-MM-dd", example="2025-07-31") LocalDate end
    ) {
        return ResponseEntity.ok(bookingService.searchBookings(userId, studioId, status, start, end));
    }

@GetMapping("/me/upcoming")
@PreAuthorize("hasRole('USER')")
public ResponseEntity<List<BookingDTO>> getMyUpcoming() {
    return ResponseEntity.ok(bookingService.getMyUpcoming());
}

    @GetMapping("/me/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BookingDTO>> getMyHistory() {
        return ResponseEntity.ok(bookingService.getMyHistory());
    }

}

