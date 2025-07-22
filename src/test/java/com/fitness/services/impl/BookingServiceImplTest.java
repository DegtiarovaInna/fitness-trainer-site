package com.fitness.services.impl;

import com.fitness.dto.BookingDTO;
import com.fitness.dto.CreateBookingRequest;
import com.fitness.dto.UpdateBookingRequest;
import com.fitness.enums.BookingStatus;
import com.fitness.exceptions.*;
import com.fitness.mappers.BookingMapper;
import com.fitness.models.Booking;
import com.fitness.models.TimeSlot;
import com.fitness.models.User;
import com.fitness.models.Studio;
import com.fitness.repositories.BookingRepository;
import com.fitness.repositories.TimeSlotRepository;
import com.fitness.repositories.UserRepository;
import com.fitness.services.interfaces.CurrentUserService;
import com.fitness.services.interfaces.EmailService;
import com.fitness.services.interfaces.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.fitness.enums.Role;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookingServiceImplTest {
    private BookingRepository bookingRepo;
    private UserRepository userRepo;
    private TimeSlotRepository slotRepo;
    private BookingMapper mapper;
    private CurrentUserService currentUserService;
    private SecurityService securityService;
    private BookingServiceImpl service;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        bookingRepo = mock(BookingRepository.class);
        userRepo = mock(UserRepository.class);
        slotRepo = mock(TimeSlotRepository.class);
        mapper = mock(BookingMapper.class);
        currentUserService = mock(CurrentUserService.class);
        securityService = mock(SecurityService.class);
        emailService = mock(EmailService.class);
        doNothing().when(securityService).requireAdminOrDev();
        when(currentUserService.getCurrentUserRole()).thenReturn(Role.ADMIN);
        service = new BookingServiceImpl(
                bookingRepo,
                userRepo,
                slotRepo,
                mapper,
                currentUserService,
                securityService,
                emailService
        );
    }

    // createBookingForCurrentUser
    @Test
    void createBookingForCurrentUser_delegates() {
        // arrange
        User me = new User();
        me.setId(50L);
        when(currentUserService.getCurrentUser()).thenReturn(me);
        when(userRepo.findById(50L)).thenReturn(Optional.of(me));

        TimeSlot slot = new TimeSlot();
        slot.setId(60L);
        slot.setDate(LocalDate.now());
        slot.setStartTime(LocalTime.of(10, 0));
        slot.setEndTime(LocalTime.of(11, 0));
        slot.setTrial(false);
        slot.setStudio(new Studio());
        when(slotRepo.findById(60L)).thenReturn(Optional.of(slot));

        when(bookingRepo.existsByTimeSlotIdAndStatusNot(60L, BookingStatus.CANCELLED))
                .thenReturn(false);
        when(bookingRepo.findByTimeSlot_DateAndStatusNot(slot.getDate(), BookingStatus.CANCELLED))
                .thenReturn(Collections.emptyList());

        Booking saved = Booking.builder()
                .id(70L)
                .timeSlot(slot)
                .build();
        when(bookingRepo.save(any(Booking.class))).thenReturn(saved);

        BookingDTO out = new BookingDTO();
        out.setId(70L);
        when(mapper.bookingToBookingDTO(saved)).thenReturn(out);

        BookingDTO result = service.createBookingForCurrentUser(60L);

        verify(currentUserService).getCurrentUser();
        verify(bookingRepo).save(any(Booking.class));
        assertSame(out, result);
    }

    // createBooking
    @Test
    void createBooking_userNotFound() {
        CreateBookingRequest req = new CreateBookingRequest();
        req.setUserId(1L);
        req.setTimeSlotId(2L);

        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> service.createBooking(req));
    }

    @Test
    void createBooking_slotNotFound() {
        CreateBookingRequest req = new CreateBookingRequest();
        req.setUserId(1L);
        req.setTimeSlotId(2L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(new User()));
        when(slotRepo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(TimeSlotNotFoundException.class,
                () -> service.createBooking(req));
    }

    @Test
    void createBooking_slotNotAvailable() {
        CreateBookingRequest req = new CreateBookingRequest();
        req.setUserId(1L);
        req.setTimeSlotId(2L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(new User()));

        TimeSlot slot = new TimeSlot();
        slot.setId(2L);
        when(slotRepo.findById(2L)).thenReturn(Optional.of(slot));

        when(bookingRepo.existsByTimeSlotIdAndStatusNot(2L, BookingStatus.CANCELLED))
                .thenReturn(true);

        assertThrows(TimeSlotNotAvailableException.class,
                () -> service.createBooking(req));
    }

    @Test
    void createBooking_trialExceeded() {
        CreateBookingRequest req = new CreateBookingRequest();
        req.setUserId(1L);
        req.setTimeSlotId(2L);

        User user = new User();
        user.setId(1L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        TimeSlot slot = new TimeSlot();
        slot.setId(2L);
        slot.setTrial(true);
        slot.setDate(LocalDate.now());
        when(slotRepo.findById(2L)).thenReturn(Optional.of(slot));

        when(bookingRepo.existsByTimeSlotIdAndStatusNot(2L, BookingStatus.CANCELLED))
                .thenReturn(false);

        LocalDate oneYearAgo = slot.getDate().minusYears(1);
        when(bookingRepo.existsByUserIdAndTimeSlot_TrialTrueAndTimeSlot_DateAfter(1L, oneYearAgo))
                .thenReturn(true);

        when(bookingRepo.findByTimeSlot_DateAndStatusNot(any(), any()))
                .thenReturn(Collections.emptyList());

        assertThrows(TrialSessionLimitExceededException.class,
                () -> service.createBooking(req));
    }

    @Test
    void createBooking_trainerNotAvailable_sameStudio() {
        CreateBookingRequest req = new CreateBookingRequest();
        req.setUserId(1L);
        req.setTimeSlotId(3L);

        User user = new User();
        user.setId(1L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        TimeSlot slot = new TimeSlot();
        slot.setId(3L);
        slot.setDate(LocalDate.now());
        slot.setStartTime(LocalTime.of(10, 0));
        slot.setEndTime(LocalTime.of(11, 0));
        slot.setTrial(false);
        Studio st = new Studio();
        st.setId(88L);
        slot.setStudio(st);
        when(slotRepo.findById(3L)).thenReturn(Optional.of(slot));

        when(bookingRepo.existsByTimeSlotIdAndStatusNot(3L, BookingStatus.CANCELLED))
                .thenReturn(false);

        Booking b = new Booking();
        TimeSlot ex = new TimeSlot();
        ex.setStudio(st);
        ex.setDate(slot.getDate());
        ex.setStartTime(LocalTime.of(10, 30));
        ex.setEndTime(LocalTime.of(11, 30));
        b.setTimeSlot(ex);

        when(bookingRepo.findByTimeSlot_DateAndStatusNot(slot.getDate(), BookingStatus.CANCELLED))
                .thenReturn(List.of(b));

        assertThrows(TrainerNotAvailableException.class,
                () -> service.createBooking(req));
    }

    @Test
    void createBooking_successful() {
        CreateBookingRequest req = new CreateBookingRequest();
        req.setUserId(2L);
        req.setTimeSlotId(4L);

        User user = new User();
        user.setId(2L);
        when(userRepo.findById(2L)).thenReturn(Optional.of(user));

        TimeSlot slot = new TimeSlot();
        slot.setId(4L);
        slot.setDate(LocalDate.now());
        slot.setStartTime(LocalTime.of(12, 0));
        slot.setEndTime(LocalTime.of(13, 0));
        slot.setTrial(false);
        slot.setStudio(new Studio());
        when(slotRepo.findById(4L)).thenReturn(Optional.of(slot));

        when(bookingRepo.existsByTimeSlotIdAndStatusNot(4L, BookingStatus.CANCELLED))
                .thenReturn(false);
        when(bookingRepo.findByTimeSlot_DateAndStatusNot(slot.getDate(), BookingStatus.CANCELLED))
                .thenReturn(Collections.emptyList());

        Booking saved = Booking.builder()
                .id(5L)
                .timeSlot(slot)
                .build();
        when(bookingRepo.save(any(Booking.class))).thenReturn(saved);

        BookingDTO out = new BookingDTO();
        out.setId(5L);
        when(mapper.bookingToBookingDTO(saved)).thenReturn(out);

        BookingDTO result = service.createBooking(req);

        assertSame(out, result);
    }

    // getBooking
    @Test
    void getBooking_missing() {
        when(bookingRepo.findById(10L)).thenReturn(Optional.empty());
        assertThrows(BookingNotFoundException.class,
                () -> service.getBooking(10L));
    }

    @Test
    void getBooking_exists() {
        Booking b = new Booking();
        b.setId(11L);
        when(bookingRepo.findById(11L)).thenReturn(Optional.of(b));
        when(currentUserService.getCurrentUserRole()).thenReturn(Role.ADMIN);

        BookingDTO dto = new BookingDTO();
        dto.setId(11L);
        when(mapper.bookingToBookingDTO(b)).thenReturn(dto);

        assertSame(dto, service.getBooking(11L));
    }

    // getAllBookings
    @Test
    void getAllBookings_maps() {
        Booking b1 = new Booking();
        b1.setId(1L);
        Booking b2 = new Booking();
        b2.setId(2L);
        when(bookingRepo.findAll()).thenReturn(List.of(b1, b2));

        BookingDTO d1 = new BookingDTO();
        d1.setId(1L);
        BookingDTO d2 = new BookingDTO();
        d2.setId(2L);
        when(mapper.bookingToBookingDTO(b1)).thenReturn(d1);
        when(mapper.bookingToBookingDTO(b2)).thenReturn(d2);

        var list = service.getAllBookings();
        assertEquals(List.of(d1, d2), list);
    }

    // cancelBooking
    @Test
    void cancelBooking_missing() {
        when(bookingRepo.findById(20L)).thenReturn(Optional.empty());
        assertThrows(BookingNotFoundException.class,
                () -> service.cancelBooking(20L));
    }

    @Test
    void cancelBooking_pending() {
        Booking b = new Booking();
        b.setId(21L);
        b.setStatus(BookingStatus.PENDING);
        TimeSlot ts = new TimeSlot();
        ts.setId(30L);
        b.setTimeSlot(ts);

        when(bookingRepo.findById(21L)).thenReturn(Optional.of(b));
        when(bookingRepo.save(b)).thenReturn(b);
        when(mapper.bookingToBookingDTO(b)).thenReturn(new BookingDTO());

        service.cancelBooking(21L);
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
    }

    @Test
    void cancelBooking_alreadyCancelled() {
        Booking b = new Booking();
        b.setId(22L);
        b.setStatus(BookingStatus.CANCELLED);
        TimeSlot ts = new TimeSlot();
        ts.setId(31L);
        b.setTimeSlot(ts);

        when(bookingRepo.findById(22L)).thenReturn(Optional.of(b));
        when(mapper.bookingToBookingDTO(b)).thenReturn(new BookingDTO());

        service.cancelBooking(22L);
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
    }

    // updateBooking
    @Test
    void updateBooking_missing() {
        assertThrows(BookingNotFoundException.class,
                () -> service.updateBooking(50L, new UpdateBookingRequest()));
    }

    @Test
    void updateBooking_statusOnly() {
        Booking b = new Booking();
        b.setId(51L);
        b.setStatus(BookingStatus.PENDING);
        TimeSlot ts = new TimeSlot();
        ts.setId(40L);
        b.setTimeSlot(ts);

        when(bookingRepo.findById(51L)).thenReturn(Optional.of(b));
        UpdateBookingRequest req = new UpdateBookingRequest();
        req.setStatus(BookingStatus.CANCELLED);

        when(bookingRepo.save(b)).thenReturn(b);
        when(mapper.bookingToBookingDTO(b)).thenReturn(new BookingDTO());

        var result = service.updateBooking(51L, req);
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
    }

    // searchBookings
    @Test
    void searchBookings_filters() {
        TimeSlot slot = new TimeSlot();
        slot.setId(1L);
        slot.setDate(LocalDate.of(2025, 1, 1));
        slot.setStudio(new Studio());
        slot.getStudio().setId(100L);

        Booking a = Booking.builder()
                .id(1L)
                .timeSlot(slot)
                .user(new User())
                .status(BookingStatus.CONFIRMED)
                .build();
        Booking b = Booking.builder()
                .id(2L)
                .timeSlot(slot)
                .user(new User())
                .status(BookingStatus.PENDING)
                .build();

        when(bookingRepo.findAll()).thenReturn(List.of(a, b));
        when(mapper.bookingToBookingDTO(a)).thenReturn(new BookingDTO());

        var list = service.searchBookings(
                null, 100L, BookingStatus.CONFIRMED,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1)
        );
        assertEquals(1, list.size());
    }

    // getMyHistory
    @Test
    void getMyHistory_maps() {
        when(currentUserService.getCurrentUserId()).thenReturn(200L);

        Booking b = new Booking();
        b.setId(200L);
        when(bookingRepo.findByUserId(200L)).thenReturn(List.of(b));

        BookingDTO dto = new BookingDTO();
        dto.setId(200L);
        when(mapper.bookingToBookingDTO(b)).thenReturn(dto);

        var list = service.getMyHistory();
        assertEquals(List.of(dto), list);
    }

    // getMyUpcoming
    @Test
    void getMyUpcoming_maps() {
        when(currentUserService.getCurrentUserId()).thenReturn(300L);
        when(currentUserService.getCurrentUserRole()).thenReturn(Role.USER);

        TimeSlot slot = new TimeSlot();
        slot.setDate(LocalDate.now().plusDays(1));
        slot.setStartTime(LocalTime.of(10, 0));
        slot.setEndTime(LocalTime.of(11, 0));
        slot.setStudio(new Studio());

        Booking b = Booking.builder()
                .id(300L)
                .status(BookingStatus.CONFIRMED)
                .user(new User())
                .timeSlot(slot)
                .build();
        b.getUser().setId(300L);

        when(bookingRepo.findAll()).thenReturn(List.of(b));

        BookingDTO dto = new BookingDTO();
        dto.setId(300L);
        when(mapper.bookingToBookingDTO(b)).thenReturn(dto);

        var list = service.getMyUpcoming();
        assertEquals(List.of(dto), list);
    }
}

