package com.fitness.services.interfaces;

public interface PasswordResetService {
    void requestReset(String email);
    void resetPassword(String token, String newPassword);
}
