package com.fitness.services.impl;

import com.fitness.config.security.JwtService;
import com.fitness.dto.AuthResponse;
import com.fitness.exceptions.EmailNotConfirmedException;
import com.fitness.exceptions.RefreshTokenException;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.models.RefreshToken;
import com.fitness.models.User;
import com.fitness.repositories.RefreshTokenRepository;
import com.fitness.repositories.UserRepository;
import com.fitness.services.interfaces.AuthenticationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepo;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AuthResponse login(String email, String password) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!user.isEnabled()) {
            throw new EmailNotConfirmedException(ErrorMessage.EMAIL_NOT_CONFIRMED);
        }

        refreshTokenRepo.revokeAllActive(email);
        String accessToken = jwtService.generateToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        refreshTokenRepo.save(RefreshToken.builder()
                .username(email)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now()
                        .plus(Duration.ofMillis(jwtService.getRefreshExpiration())))
                .build());

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new RefreshTokenException(ErrorMessage.INVALID_REFRESH);
        }
        RefreshToken rt = refreshTokenRepo.findByToken(refreshToken)
                .orElseThrow(() -> new  RefreshTokenException(ErrorMessage.INVALID_REFRESH));

        if (rt.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepo.delete(rt);
            throw new  RefreshTokenException(ErrorMessage.INVALID_REFRESH);
        }

        if (rt.isRevoked()) {
            rt.setReused(true);
            refreshTokenRepo.save(rt);
            refreshTokenRepo.revokeAllActive(rt.getUsername());
            throw new RefreshTokenException(ErrorMessage.REFRESH_TOKEN_REUSED);
        }

        rt.setRevoked(true);
        refreshTokenRepo.save(rt);

        String username = rt.getUsername();
        String newAccessToken = jwtService.generateToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);

        refreshTokenRepo.save(RefreshToken.builder()
                .username(username)
                .token(newRefreshToken)
                .expiresAt(LocalDateTime.now()
                        .plus(Duration.ofMillis(jwtService.getRefreshExpiration())))
                .build());

        return new AuthResponse(newAccessToken, newRefreshToken);
    }
}
