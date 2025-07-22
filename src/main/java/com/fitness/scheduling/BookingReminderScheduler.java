package com.fitness.scheduling;

import com.fitness.enums.BookingStatus;
import com.fitness.repositories.BookingRepository;
import com.fitness.services.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class BookingReminderScheduler {
    private final BookingRepository bookingRepo;
    private final EmailService emailService;


    @Scheduled(cron = "0 15 3 * * *")
    public void sendTomorrowReminders() {

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        bookingRepo.findByTimeSlot_DateAndStatus(tomorrow, BookingStatus.CONFIRMED)
                .forEach(b -> emailService.sendBookingReminderEmail(b.getUser(), b));
    }
}
