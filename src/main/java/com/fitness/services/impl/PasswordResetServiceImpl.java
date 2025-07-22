package com.fitness.services.impl;

import com.fitness.config.security.JwtService;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.repositories.UserRepository;
import com.fitness.services.interfaces.EmailService;
import com.fitness.services.interfaces.PasswordResetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.fitness.exceptions.InvalidTokenException;

@RequiredArgsConstructor
@Service
public class PasswordResetServiceImpl implements PasswordResetService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public void requestReset(String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            return;
        }
        String token = jwtService.generateResetToken(email);
        emailService.sendPasswordResetEmail(email, token);
    }
    @Transactional
    @Override
    public void resetPassword(String token, String newPassword) {
        String email = jwtService.extractUsername(token);
        UserDetails ud    = userDetailsService.loadUserByUsername(email);
        if (!jwtService.isResetToken(token, ud)) {
            throw new InvalidTokenException(ErrorMessage.INVALID_OR_EXPIRED_TOKEN);
        }
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException(ErrorMessage.INVALID_OR_EXPIRED_TOKEN));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}

