package com.fitness.services.interfaces;

import com.fitness.dto.AuthResponse;

public interface AuthenticationService {
    AuthResponse login(String email, String password);
    AuthResponse refresh(String refreshToken);
}
