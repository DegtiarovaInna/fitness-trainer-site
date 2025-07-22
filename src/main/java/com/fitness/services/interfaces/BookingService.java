package com.fitness.services.interfaces;

import com.fitness.dto.BookingDTO;
import com.fitness.dto.CreateBookingRequest;
import com.fitness.dto.UpdateBookingRequest;

import java.util.List;

public interface BookingService {
    BookingDTO createBookingForCurrentUser(Long timeSlotId);
    BookingDTO createBooking(CreateBookingRequest req);
    BookingDTO getBooking(Long id);
    List<BookingDTO> getAllBookings();
    BookingDTO cancelBooking(Long bookingId);
    BookingDTO updateBooking(Long bookingId, UpdateBookingRequest req);
   // List<BookingDTO> getBookingsByUser(Long userId);
    List<BookingDTO> searchBookings(
            Long userId, Long studioId,
            com.fitness.enums.BookingStatus status,
            java.time.LocalDate start, java.time.LocalDate end);
    List<BookingDTO> getMyHistory();
    List<BookingDTO> getMyUpcoming();
}
