package com.fitness.services.impl;

import com.fitness.config.security.JwtService;
import com.fitness.dto.AuthResponse;
import com.fitness.exceptions.RefreshTokenException;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.models.RefreshToken;
import com.fitness.models.User;
import com.fitness.repositories.RefreshTokenRepository;
import com.fitness.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class AuthenticationServiceImplTest {
    private AuthenticationManager authManager;
    private JwtService jwtService;
    private RefreshTokenRepository refreshTokenRepo;
    private UserRepository userRepository;
    private AuthenticationServiceImpl service;

    @BeforeEach
    void setUp() {
        authManager       = mock(AuthenticationManager.class);
        jwtService        = mock(JwtService.class);
        refreshTokenRepo  = mock(RefreshTokenRepository.class);
        userRepository    = mock(UserRepository.class);
        service = new AuthenticationServiceImpl(authManager,
                jwtService,
                refreshTokenRepo,
                userRepository);
    }


    // login
    @Test
    void login_successful() {
        String email = "user@example.com";
        String password = "pass";
        String access  = "access-token";
        String refresh = "refresh-token";
        long   refreshExp = 5_000L;

        User u = new User(); u.setEmail(email); u.setEnabled(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(u));

        when(jwtService.generateToken(email)).thenReturn(access);
        when(jwtService.generateRefreshToken(email)).thenReturn(refresh);
        when(jwtService.getRefreshExpiration()).thenReturn(refreshExp);

        AuthResponse resp = service.login(email, password);

        verify(authManager).authenticate(new UsernamePasswordAuthenticationToken(email, password));

        verify(refreshTokenRepo).revokeAllActive(email);

        verify(refreshTokenRepo).save(argThat(rt ->
                rt.getUsername().equals(email) &&
                        rt.getToken().equals(refresh) &&
                        rt.getExpiresAt().isAfter(LocalDateTime.now())
        ));
        assertEquals(access,  resp.getAccessToken());
        assertEquals(refresh, resp.getRefreshToken());
    }

    @Test
    void login_authenticationFails_throws() {
        doThrow(new BadCredentialsException("bad"))
                .when(authManager).authenticate(any());
        assertThrows(BadCredentialsException.class,
                () -> service.login("e","p"));
        verifyNoMoreInteractions(jwtService, refreshTokenRepo);
    }


    @Test
    void refresh_notRefreshToken_throws() {
        when(jwtService.isRefreshToken("r")).thenReturn(false);
        assertThrows(RefreshTokenException.class,
                () -> service.refresh("r"),
                "Expected INVALID_REFRESH"
        );
    }

    @Test
    void refresh_tokenNotFound_throws() {
        when(jwtService.isRefreshToken("r")).thenReturn(true);
        when(refreshTokenRepo.findByToken("r")).thenReturn(Optional.empty());
        RefreshTokenException ex = assertThrows(RefreshTokenException.class,
                () -> service.refresh("r")
        );
        assertEquals(ErrorMessage.INVALID_REFRESH, ex.getMessage());
    }

    @Test
    void refresh_tokenExpired_throwsAndDeletes() {
        when(jwtService.isRefreshToken("r")).thenReturn(true);
        RefreshToken rt = new RefreshToken();
        rt.setUsername("u");
        rt.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        when(refreshTokenRepo.findByToken("r")).thenReturn(Optional.of(rt));

        assertThrows(RefreshTokenException.class,
                () -> service.refresh("r")
        );
        verify(refreshTokenRepo).delete(rt);
    }

    @Test
    void refresh_successful() {
        when(jwtService.isRefreshToken("old")).thenReturn(true);

        RefreshToken stored = new RefreshToken();
        stored.setUsername("u");
        stored.setToken("old");
        stored.setExpiresAt(LocalDateTime.now().plusSeconds(60));
        when(refreshTokenRepo.findByToken("old"))
                .thenReturn(Optional.of(stored));

        when(jwtService.generateToken("u")).thenReturn("new-access");
        when(jwtService.generateRefreshToken("u")).thenReturn("new-refresh");
        when(jwtService.getRefreshExpiration()).thenReturn(1_000L);
        AuthResponse resp = service.refresh("old");

        verify(refreshTokenRepo).save(argThat(rt ->
                rt.isRevoked() && "old".equals(rt.getToken())));
        verify(refreshTokenRepo).save(argThat(rt ->
                "new-refresh".equals(rt.getToken())));

        assertEquals("new-access",  resp.getAccessToken());
        assertEquals("new-refresh", resp.getRefreshToken());
    }
}
