package com.fitness.services.interfaces;

import com.fitness.models.Booking;
import com.fitness.models.User;

public interface EmailService {
    void sendRegistrationEmail(String to, String token);
    void sendProfileUpdateEmail(String to);
    void sendPasswordChangedEmail(String to);
    void sendPasswordResetEmail(String to, String token);



    void sendBookingConfirmationEmail(User to, Booking booking);
    void sendBookingCancellationEmail(User to, Booking booking);
    void sendBookingReminderEmail   (User to, Booking booking);


    void sendGoodbyeEmail(String to);

}
