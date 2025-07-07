package com.fitness.services.interfaces;

public interface EmailService {
    void sendRegistrationEmail(String to, String token);
    void sendProfileUpdateEmail(String to);
}
