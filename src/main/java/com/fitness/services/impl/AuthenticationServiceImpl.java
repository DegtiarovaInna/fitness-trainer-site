package com.fitness.services.impl;

import com.fitness.config.security.JwtService;
import com.fitness.dto.AuthResponse;
import com.fitness.exceptions.EmailNotConfirmedException;
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
        String accessToken  = jwtService.generateToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        refreshTokenRepo.deleteByUsername(email);
        RefreshToken rt = new RefreshToken();
        rt.setUsername(email);
        rt.setToken(refreshToken);
        rt.setExpiresAt(
                LocalDateTime.now()
                        .plus(Duration.ofMillis(jwtService.getRefreshExpiration()))
        );
        refreshTokenRepo.save(rt);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException(ErrorMessage.INVALID_REFRESH);
        }
        RefreshToken rt = refreshTokenRepo.findByToken(refreshToken)
                .orElseThrow(() -> new BadCredentialsException(ErrorMessage.INVALID_REFRESH));
        if (rt.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepo.delete(rt);
            throw new BadCredentialsException(ErrorMessage.INVALID_REFRESH);
        }

        String newAccessToken = jwtService.generateToken(rt.getUsername());
        return new AuthResponse(newAccessToken, refreshToken);
    }
}
