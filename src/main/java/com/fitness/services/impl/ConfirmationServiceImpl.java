package com.fitness.services.impl;

import com.fitness.config.security.JwtService;
import com.fitness.exceptions.AlreadyConfirmedException;
import com.fitness.exceptions.InvalidTokenException;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.models.User;
import com.fitness.repositories.UserRepository;
import com.fitness.services.interfaces.ConfirmationService;
import com.fitness.services.interfaces.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfirmationServiceImpl implements ConfirmationService {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    @Override
    @Transactional
    public void confirmToken(String token) {

        String email = jwtService.extractUsername(token);

        UserDetails ud = userDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(token, ud)) {
            throw new InvalidTokenException(ErrorMessage.INVALID_OR_EXPIRED_TOKEN);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException(ErrorMessage.INVALID_OR_EXPIRED_TOKEN));

        if (Boolean.TRUE.equals(user.isEnabled())) {
            throw new AlreadyConfirmedException(ErrorMessage.EMAIL_IS_ALREADY_CONFIRMED);
        }

        user.setEnabled(true);
        userRepository.save(user);
    }
    @Override
    public void resendConfirmationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException(ErrorMessage.USER_NOT_FOUND));
        if (user.isEnabled()) {
            throw new AlreadyConfirmedException(ErrorMessage.EMAIL_IS_ALREADY_CONFIRMED);
        }
        String token = jwtService.generateToken(email);
        emailService.sendRegistrationEmail(email, token);
    }
}
