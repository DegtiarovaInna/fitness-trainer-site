package com.fitness.services.impl;


import com.fitness.dto.BookingDTO;
import com.fitness.dto.CreateBookingRequest;
import com.fitness.dto.UpdateBookingRequest;
import com.fitness.enums.BookingStatus;
import com.fitness.exceptions.*;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.mappers.BookingMapper;
import com.fitness.models.Booking;
import com.fitness.models.TimeSlot;
import com.fitness.models.User;
import com.fitness.repositories.BookingRepository;
import com.fitness.repositories.TimeSlotRepository;
import com.fitness.repositories.UserRepository;
import com.fitness.services.interfaces.CurrentUserService;
import com.fitness.services.interfaces.BookingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import com.fitness.services.interfaces.SecurityService;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    private final UserRepository userRepository;


    private final TimeSlotRepository timeSlotRepository;

    private final BookingMapper bookingMapper;

    private final CurrentUserService currentUserService;
    private final SecurityService securityService;
@Override
    @Transactional
    public BookingDTO createBookingForCurrentUser(Long timeSlotId) {
        User me = currentUserService.getCurrentUser();
        CreateBookingRequest req = new CreateBookingRequest();
        req.setUserId(me.getId());
        req.setTimeSlotId(timeSlotId);
        return createBooking(req);
    }
    @Override
    @Transactional
    public BookingDTO createBooking(CreateBookingRequest req) {
        securityService.requireAdminOrDev();

        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND));

        TimeSlot slot = timeSlotRepository.findById(req.getTimeSlotId())
                .orElseThrow(() -> new TimeSlotNotFoundException(ErrorMessage.TIME_SLOT_NOT_FOUND));


        if (bookingRepository.existsByTimeSlotIdAndStatusNot(
                slot.getId(), BookingStatus.CANCELLED)) {
            throw new TimeSlotNotAvailableException(ErrorMessage.TIME_SLOT_NOT_AVAILABLE);
        }

        if (slot.isTrial()) {
            LocalDate oneYearAgo = slot.getDate().minusYears(1);
            if (bookingRepository.existsByUserIdAndTimeSlot_TrialTrueAndTimeSlot_DateAfter(
                    user.getId(), oneYearAgo)) {
                throw new TrialSessionLimitExceededException(
                        ErrorMessage.TRIAL_SESSION_LIMIT_EXCEEDED);
            }
        }

        List<Booking> todays = bookingRepository
                .findByTimeSlot_DateAndStatusNot(slot.getDate(), BookingStatus.CANCELLED);

        LocalTime start = slot.getStartTime();
        LocalTime end   = slot.getEndTime();

        for (Booking b : todays) {
            TimeSlot ex = b.getTimeSlot();

            if (!ex.getStudio().getId().equals(slot.getStudio().getId())) {

                boolean ok = ! start.isBefore(ex.getEndTime().plusHours(1))
                        || ! end.isAfter(ex.getStartTime().minusHours(1));
                if (!ok) {
                    throw new TrainerNotAvailableException(
                            ErrorMessage.TRAINER_NOT_AVAILABLE);
                }
            } else {

                boolean ok = end.isBefore(ex.getStartTime())
                        || !start.isBefore(ex.getEndTime());
                if (!ok) {
                    throw new TrainerNotAvailableException(
                            ErrorMessage.TRAINER_NOT_AVAILABLE);
                }
            }
        }

        Booking booking = Booking.builder()
                .user(user)
                .timeSlot(slot)
                .status(BookingStatus.PENDING)
                .build();

        Booking saved = bookingRepository.save(booking);

        TimeSlot ts = saved.getTimeSlot();
        ts.setAvailable(false);
        timeSlotRepository.save(ts);

        return bookingMapper.bookingToBookingDTO(saved);
    }
    @Override
    public BookingDTO getBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(ErrorMessage.BOOKING_NOT_FOUND));
        return bookingMapper.bookingToBookingDTO(booking);
    }
    @Override
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(bookingMapper::bookingToBookingDTO)
                .collect(Collectors.toList());
    }
    @Override
    public BookingDTO cancelBooking(Long bookingId) {
        securityService.requireAdminOrDev();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(ErrorMessage.BOOKING_NOT_FOUND));

        if (booking.getStatus() != BookingStatus.CANCELLED) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking = bookingRepository.save(booking);
        }

        TimeSlot slot = booking.getTimeSlot();
        slot.setAvailable(true);
        timeSlotRepository.save(slot);

        return bookingMapper.bookingToBookingDTO(booking);
    }
    @Override
@Transactional
public BookingDTO updateBooking(Long bookingId, UpdateBookingRequest req) {
        securityService.requireAdminOrDev();

    Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingNotFoundException(ErrorMessage.BOOKING_NOT_FOUND));

    TimeSlot oldSlot = booking.getTimeSlot();
    BookingStatus oldStatus = booking.getStatus();

    if (req.getTimeSlotId() != null
            && !req.getTimeSlotId().equals(oldSlot.getId())
    ) {
        TimeSlot newSlot = timeSlotRepository.findById(req.getTimeSlotId())
                .orElseThrow(() -> new TimeSlotNotFoundException(ErrorMessage.TIME_SLOT_NOT_FOUND));

        if (bookingRepository.existsByTimeSlotIdAndStatusNot(
                newSlot.getId(), BookingStatus.CANCELLED
        )) {
            throw new TimeSlotNotAvailableException(ErrorMessage.TIME_SLOT_NOT_AVAILABLE);
        }
        List<Booking> todays = bookingRepository
                .findByTimeSlot_DateAndStatusNot(newSlot.getDate(), BookingStatus.CANCELLED);

        LocalTime start = newSlot.getStartTime();
        LocalTime end   = newSlot.getEndTime();
        for (Booking b : todays) {
            if (b.getId().equals(bookingId)) continue;
            TimeSlot ex = b.getTimeSlot();
            if (!ex.getStudio().getId().equals(newSlot.getStudio().getId())) {
                boolean ok = ! start.isBefore(ex.getEndTime().plusHours(1))
                        || ! end.isAfter(ex.getStartTime().minusHours(1));
                if (!ok) throw new TrainerNotAvailableException(ErrorMessage.TRAINER_NOT_AVAILABLE);
            } else {
                boolean ok = end.isBefore(ex.getStartTime())
                        || !start.isBefore(ex.getEndTime());
                if (!ok) throw new TrainerNotAvailableException(ErrorMessage.TRAINER_NOT_AVAILABLE);
            }
        }

        oldSlot.setAvailable(true);
        timeSlotRepository.save(oldSlot);

        booking.setTimeSlot(newSlot);
        newSlot.setAvailable(false);
        timeSlotRepository.save(newSlot);
    }

    if (req.getStatus() != null) {
        booking.setStatus(req.getStatus());

        if (req.getStatus() == BookingStatus.CANCELLED) {
            TimeSlot ts = booking.getTimeSlot();
            ts.setAvailable(true);
            timeSlotRepository.save(ts);
        }
        if (req.getStatus() != BookingStatus.CANCELLED
                && oldStatus == BookingStatus.CANCELLED) {
            TimeSlot ts = booking.getTimeSlot();
            ts.setAvailable(false);
            timeSlotRepository.save(ts);
        }
    }

    Booking updated = bookingRepository.save(booking);
    return bookingMapper.bookingToBookingDTO(updated);
}
    @Override
    public List<BookingDTO> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(bookingMapper::bookingToBookingDTO)
                .collect(Collectors.toList());
    }
    @Override
    public List<BookingDTO> searchBookings(
            Long userId,
            Long studioId,
            BookingStatus status,
            LocalDate start,
            LocalDate end
    ) {
        return bookingRepository.findAll().stream()

                .filter(b -> userId == null || b.getUser().getId().equals(userId))

                .filter(b -> studioId == null || b.getTimeSlot().getStudio().getId().equals(studioId))

                .filter(b -> status    == null || b.getStatus() == status)

                .filter(b -> start     == null || !b.getTimeSlot().getDate().isBefore(start))

                .filter(b -> end       == null || !b.getTimeSlot().getDate().isAfter(end))
                .map(bookingMapper::bookingToBookingDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getMyHistory() {
        Long me = currentUserService.getCurrentUserId();
        return bookingRepository.findByUserId(me).stream()
                .map(bookingMapper::bookingToBookingDTO)
                .toList();
    }


    @Override
    public List<BookingDTO> getMyUpcoming() {
        Long me = currentUserService.getCurrentUserId();

        List<BookingStatus> active = List.of(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED
        );
        return bookingRepository
                .findByUserIdAndStatusInAndTimeSlot_DateAfter(me, active, LocalDate.now().minusDays(1))
                .stream()
                .map(bookingMapper::bookingToBookingDTO)
                .toList();
    }

}
