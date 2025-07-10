package com.fitness.services.impl;

import com.fitness.config.security.JwtService;
import com.fitness.dto.AuthResponse;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.models.RefreshToken;
import com.fitness.repositories.RefreshTokenRepository;
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
    private AuthenticationServiceImpl service;

    @BeforeEach
    void setUp() {
        authManager       = mock(AuthenticationManager.class);
        jwtService        = mock(JwtService.class);
        refreshTokenRepo  = mock(RefreshTokenRepository.class);
        service = new AuthenticationServiceImpl(authManager, jwtService, refreshTokenRepo);
    }

    // login
    @Test
    void login_successful() {
        String email = "user@example.com";
        String password = "pass";
        String access = "access-token";
        String refresh = "refresh-token";
        long refreshExp = 5_000L;

        when(jwtService.generateToken(email)).thenReturn(access);
        when(jwtService.generateRefreshToken(email)).thenReturn(refresh);
        when(jwtService.getRefreshExpiration()).thenReturn(refreshExp);

        AuthResponse resp = service.login(email, password);

        verify(authManager).authenticate(new UsernamePasswordAuthenticationToken(email, password));
        verify(refreshTokenRepo).deleteByUsername(email);
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
        assertThrows(BadCredentialsException.class,
                () -> service.refresh("r"),
                "Expected INVALID_REFRESH"
        );
    }

    @Test
    void refresh_tokenNotFound_throws() {
        when(jwtService.isRefreshToken("r")).thenReturn(true);
        when(refreshTokenRepo.findByToken("r")).thenReturn(Optional.empty());
        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
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

        assertThrows(BadCredentialsException.class,
                () -> service.refresh("r")
        );
        verify(refreshTokenRepo).delete(rt);
    }

    @Test
    void refresh_successful() {
        when(jwtService.isRefreshToken("r")).thenReturn(true);
        RefreshToken rt = new RefreshToken();
        rt.setUsername("u");
        rt.setExpiresAt(LocalDateTime.now().plusSeconds(60));
        when(refreshTokenRepo.findByToken("r")).thenReturn(Optional.of(rt));
        when(jwtService.generateToken("u")).thenReturn("new-access");

        AuthResponse resp = service.refresh("r");

        assertEquals("new-access", resp.getAccessToken());
        assertEquals("r",          resp.getRefreshToken());
    }
}
