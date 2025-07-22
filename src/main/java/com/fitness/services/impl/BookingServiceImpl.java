package com.fitness.services.impl;


import com.fitness.dto.BookingDTO;
import com.fitness.dto.CreateBookingRequest;
import com.fitness.dto.UpdateBookingRequest;
import com.fitness.enums.BookingStatus;
import com.fitness.enums.Role;
import org.springframework.security.access.AccessDeniedException;
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
import com.fitness.services.interfaces.EmailService;
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
    private static final long INTER_STUDIO_BUFFER_HOURS = 1L;

    private final BookingRepository bookingRepository;

    private final UserRepository userRepository;


    private final TimeSlotRepository timeSlotRepository;

    private final BookingMapper bookingMapper;

    private final CurrentUserService currentUserService;
    private final SecurityService securityService;
    private final EmailService emailService;

    @Override
    @Transactional
    public BookingDTO createBookingForCurrentUser(Long timeSlotId) {

        User me = currentUserService.getCurrentUser();
        TimeSlot slot = getSlotOrThrow(timeSlotId);

        Booking created = createInternal(me, slot);
        return bookingMapper.bookingToBookingDTO(created);
    }

    @Override
    @Transactional
    public BookingDTO createBooking(CreateBookingRequest req) {

        securityService.requireAdminOrDev();
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND));
        TimeSlot slot = getSlotOrThrow(req.getTimeSlotId());

        Booking created = createInternal(user, slot);
        return bookingMapper.bookingToBookingDTO(created);
    }

    private Booking createInternal(User user, TimeSlot slot) {


        if (bookingRepository.existsByTimeSlotIdAndStatusNot(
                slot.getId(), BookingStatus.CANCELLED)) {
            throw new TimeSlotNotAvailableException(ErrorMessage.TIME_SLOT_NOT_AVAILABLE);
        }

        if (slot.isTrial()) {
            LocalDate oneYearAgo = slot.getDate().minusYears(1);
            if (bookingRepository.existsByUserIdAndTimeSlot_TrialTrueAndTimeSlot_DateAfter(
                    user.getId(), oneYearAgo)) {
                throw new TrialSessionLimitExceededException(ErrorMessage.TRIAL_SESSION_LIMIT_EXCEEDED);
            }
        }

        checkTrainerAvailability(slot);

        Booking booking = Booking.builder()
                .user(user)
                .timeSlot(slot)
                .status(BookingStatus.PENDING)
                .build();

        Booking saved = bookingRepository.save(booking);

        slot.setAvailable(false);
        timeSlotRepository.save(slot);

        emailService.sendBookingConfirmationEmail(user, saved);
        return saved;
    }

    private void checkTrainerAvailability(TimeSlot candidate) {
        checkTrainerAvailability(candidate, null);
    }

    private void checkTrainerAvailability(TimeSlot candidate, Long ignoreBookingId) {
        List<Booking> sameDay = bookingRepository
                .findByTimeSlot_DateAndStatusNot(candidate.getDate(), BookingStatus.CANCELLED);

        LocalTime start = candidate.getStartTime();
        LocalTime end = candidate.getEndTime();

        for (Booking b : sameDay) {
            if (ignoreBookingId != null && b.getId().equals(ignoreBookingId)) continue;
            TimeSlot ex = b.getTimeSlot();
            boolean sameStudio = ex.getStudio().getId().equals(candidate.getStudio().getId());

            if (sameStudio) {
                boolean ok = end.isBefore(ex.getStartTime())
                        || !start.isBefore(ex.getEndTime());
                if (!ok) throw new TrainerNotAvailableException(ErrorMessage.TRAINER_NOT_AVAILABLE);

            } else {
                boolean clash = start.isBefore(ex.getEndTime().plusHours(INTER_STUDIO_BUFFER_HOURS))
                        && end.isAfter(ex.getStartTime().minusHours(INTER_STUDIO_BUFFER_HOURS));
                if (clash) throw new TrainerNotAvailableException(ErrorMessage.TRAINER_NOT_AVAILABLE);
            }
        }
    }

    private TimeSlot getSlotOrThrow(Long slotId) {
        return timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new TimeSlotNotFoundException(ErrorMessage.TIME_SLOT_NOT_FOUND));
    }
    private boolean canViewBooking(Booking b) {
        Role role = currentUserService.getCurrentUserRole();
        Long me   = currentUserService.getCurrentUserId();

        return switch (role) {
            case ADMIN, DEV -> true;
            case USER -> b.getUser().getId().equals(me);
            case USER_PRO -> {
                User studioAdmin = b.getTimeSlot()
                        .getStudio()
                        .getAdmin();
                yield studioAdmin != null && studioAdmin.getId().equals(me);
            }
        };
    }
    @Override
    public BookingDTO getBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(ErrorMessage.BOOKING_NOT_FOUND));
        if (!canViewBooking(booking))
            throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED);
        return bookingMapper.bookingToBookingDTO(booking);
    }

    @Override
    public List<BookingDTO> getAllBookings() {
        securityService.requireAdminOrDev();
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
        emailService.sendBookingCancellationEmail(booking.getUser(), booking);

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
            checkTrainerAvailability(newSlot, bookingId);

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

        if (req.getStatus() == BookingStatus.CONFIRMED) {
            emailService.sendBookingConfirmationEmail(updated.getUser(), updated);
        } else if (req.getStatus() == BookingStatus.CANCELLED) {
            emailService.sendBookingCancellationEmail(updated.getUser(), updated);
        }

        return bookingMapper.bookingToBookingDTO(updated);
    }

//    @Override
//    public List<BookingDTO> getBookingsByUser(Long userId) {
//        return bookingRepository.findByUserId(userId).stream()
//                .map(bookingMapper::bookingToBookingDTO)
//                .collect(Collectors.toList());
//    }

    @Override
    public List<BookingDTO> searchBookings(
            Long userId,
            Long studioId,
            BookingStatus status,
            LocalDate start,
            LocalDate end
    ) {
        Role role = currentUserService.getCurrentUserRole();
        Long me   = currentUserService.getCurrentUserId();
        return bookingRepository.findAll().stream()
                .filter(b -> switch (role) {
                    case ADMIN, DEV -> true;
                    case USER -> b.getUser().getId().equals(me);
                    case USER_PRO -> {
                        User admin = b.getTimeSlot().getStudio().getAdmin();
                        yield admin != null && admin.getId().equals(me);
                    }
                })
                .filter(b -> userId == null || b.getUser().getId().equals(userId))

                .filter(b -> studioId == null || b.getTimeSlot().getStudio().getId().equals(studioId))

                .filter(b -> status == null || b.getStatus() == status)

                .filter(b -> start == null || !b.getTimeSlot().getDate().isBefore(start))

                .filter(b -> end == null || !b.getTimeSlot().getDate().isAfter(end))
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
        Role role = currentUserService.getCurrentUserRole();
        Long me = currentUserService.getCurrentUserId();
        LocalDate today = LocalDate.now().minusDays(1);

        List<BookingStatus> active = List.of(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED
        );
        return bookingRepository.findAll().stream()
                .filter(b -> active.contains(b.getStatus()))
                .filter(b -> !b.getTimeSlot().getDate().isBefore(today))
                .filter(b -> switch (role) {
                    case ADMIN, DEV -> true;
                    case USER -> b.getUser().getId().equals(me);
                    case USER_PRO -> {
                        User admin = b.getTimeSlot().getStudio().getAdmin();
                        yield admin != null && admin.getId().equals(me);
                    }
                })
                .map(bookingMapper::bookingToBookingDTO)
                .toList();
    }

}
