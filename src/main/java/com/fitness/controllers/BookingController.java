package com.fitness.controllers;

import com.fitness.dto.BookingDTO;
import com.fitness.dto.CreateBookingRequest;
import com.fitness.dto.CreateOwnBookingRequest;
import com.fitness.dto.UpdateBookingRequest;
import com.fitness.enums.BookingStatus;
import com.fitness.services.interfaces.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Booking", description = "Booking Management")
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Create own booking (current user)")
    @ApiResponse(
            responseCode = "200",
            description = "Created booking",
            content = @Content(schema = @Schema(implementation = BookingDTO.class))
    )
    @PostMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingDTO> createOwnBooking(
            @Valid @RequestBody CreateOwnBookingRequest req) {
        BookingDTO dto = bookingService.createBookingForCurrentUser(req.getTimeSlotId());
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Create booking for specific user (admin)")
    @ApiResponse(responseCode = "200", description = "Created",
            content = @Content(schema = @Schema(implementation = BookingDTO.class)))
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEV')")
    public ResponseEntity<BookingDTO> createBookingForUser(
            @Valid @RequestBody CreateBookingRequest req) {
        BookingDTO dto = bookingService.createBooking(req);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Get booking by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(schema = @Schema(implementation = BookingDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'USER_PRO', 'ADMIN', 'DEV')")
    public ResponseEntity<BookingDTO> getBooking(@PathVariable Long id) {
        BookingDTO bookingDTO = bookingService.getBooking(id);
        return ResponseEntity.ok(bookingDTO);
    }

    @Operation(summary = "List all bookings (admin)")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BookingDTO.class))))
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DEV')")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @Operation(summary = "Cancel booking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cancelled",
                    content = @Content(schema = @Schema(implementation = BookingDTO.class))),
            @ApiResponse(responseCode = "400", description = "Business rule violation", content = @Content)
    })
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable Long id) {
        BookingDTO bookingDTO = bookingService.cancelBooking(id);
        return ResponseEntity.ok(bookingDTO);
    }

    @Operation(summary = "Update booking (admin)")

    @ApiResponse(responseCode = "200", description = "Updated",
            content = @Content(schema = @Schema(implementation = BookingDTO.class)))
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEV')")
    public ResponseEntity<BookingDTO> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingRequest req) {
        BookingDTO dto = bookingService.updateBooking(id, req);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Search bookings",
            description = "Filter by user, studio, status and date range.")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BookingDTO.class))))
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER','USER_PRO','ADMIN','DEV')")
    public ResponseEntity<List<BookingDTO>> searchBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long studioId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @Parameter(description = "Start of period, format yyyy-MM-dd", example = "2025-07-01") LocalDate start,
            @RequestParam(required = false) @Parameter(description = "End of period, format yyyy-MM-dd", example = "2025-07-31") LocalDate end
    ) {
        return ResponseEntity.ok(bookingService.searchBookings(userId, studioId, status, start, end));
    }

    @Operation(summary = "My upcoming bookings")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BookingDTO.class))))
    @GetMapping("/me/upcoming")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BookingDTO>> getMyUpcoming() {
        return ResponseEntity.ok(bookingService.getMyUpcoming());
    }

    @Operation(summary = "My booking history")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BookingDTO.class))))
    @GetMapping("/me/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BookingDTO>> getMyHistory() {
        return ResponseEntity.ok(bookingService.getMyHistory());
    }

}

