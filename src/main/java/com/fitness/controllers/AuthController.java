package com.fitness.controllers;

import com.fitness.config.security.JwtService;
import com.fitness.dto.AuthResponse;
import com.fitness.dto.RegisterUserRequest;
import com.fitness.dto.UserDTO;
import com.fitness.models.RefreshToken;
import com.fitness.repositories.RefreshTokenRepository;
import com.fitness.services.interfaces.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepo;

    @PostMapping("/register")
    @PermitAll
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        UserDTO userDTO = userService.registerUser(registerUserRequest);
        return ResponseEntity.ok(userDTO);
    }
    @RateLimiter(name = "loginRateLimiter")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestParam String email,
            @RequestParam String password
    ) {
        // 1) Аутентификация
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        // 2) Генерация токенов
        String accessToken  = jwtService.generateToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        // 3) Сохраняем только один RefreshToken на пользователя
        refreshTokenRepo.deleteByUsername(email);

        RefreshToken rt = new RefreshToken();
        rt.setToken(refreshToken);
        rt.setUsername(email);
        // ← здесь используем Duration.ofMillis(...)
        rt.setExpiresAt(
                LocalDateTime.now()
                        .plus(Duration.ofMillis(jwtService.getRefreshExpiration()))
        );
        refreshTokenRepo.save(rt);

        // 4) Возвращаем оба токена
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestParam String refreshToken
    ) {
        // 1) Проверяем формат и «type»
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        // 2) Ищем в БД
        RefreshToken rt = refreshTokenRepo.findByToken(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Refresh token not found"));

        // 3) Проверяем срок жизни
        if (rt.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepo.delete(rt);
            throw new BadCredentialsException("Refresh token expired");
        }

        // 4) Генерируем новый access-токен
        String email = rt.getUsername();
        String newAccessToken = jwtService.generateToken(email);

        // 5) Отдаём новый access и старый refresh
        return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken));
    }
}
