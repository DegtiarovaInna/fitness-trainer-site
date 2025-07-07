package com.fitness.services.interfaces;

public interface ConfirmationService {

    void confirmToken(String token);
    void resendConfirmationEmail(String email);
}
