package com.fitness.services.impl;
import com.fitness.config.security.JwtService;
import com.fitness.exceptions.AlreadyConfirmedException;
import com.fitness.exceptions.InvalidTokenException;
import com.fitness.models.User;
import com.fitness.repositories.UserRepository;
import com.fitness.services.interfaces.EmailService;
import com.fitness.services.interfaces.ConfirmationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class ConfirmationServiceImplTest {
    private JwtService jwtService;
    private UserDetailsService userDetailsService;
    private UserRepository userRepository;
    private EmailService emailService;
    private ConfirmationService service;

    @BeforeEach
    void setUp() {
        jwtService          = mock(JwtService.class);
        userDetailsService  = mock(UserDetailsService.class);
        userRepository      = mock(UserRepository.class);
        emailService        = mock(EmailService.class);
        service = new ConfirmationServiceImpl(
                jwtService, userDetailsService, userRepository, emailService
        );
    }

    // confirmToken
    @Test
    void confirmToken_invalidSignature_throws() {
        String token = "tkn", email = "e@x";
        when(jwtService.extractUsername(token)).thenReturn(email);
        UserDetails ud = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(ud);
        when(jwtService.isTokenValid(token, ud)).thenReturn(false);

        assertThrows(InvalidTokenException.class,
                () -> service.confirmToken(token),
                "Expected INVALID_OR_EXPIRED_TOKEN"
        );
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void confirmToken_userNotFound_throws() {
        String token = "t", email = "a@b";
        when(jwtService.extractUsername(token)).thenReturn(email);
        UserDetails ud = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(ud);
        when(jwtService.isTokenValid(token, ud)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class,
                () -> service.confirmToken(token)
        );
    }

    @Test
    void confirmToken_alreadyConfirmed_throws() {
        String token = "t", email = "x@y";
        when(jwtService.extractUsername(token)).thenReturn(email);
        UserDetails ud = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(ud);
        when(jwtService.isTokenValid(token, ud)).thenReturn(true);

        User user = new User();
        user.setEnabled(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(AlreadyConfirmedException.class,
                () -> service.confirmToken(token)
        );
    }

    @Test
    void confirmToken_success_enablesUser() {
        String token = "tk", email = "u@u";
        when(jwtService.extractUsername(token)).thenReturn(email);
        UserDetails ud = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(ud);
        when(jwtService.isTokenValid(token, ud)).thenReturn(true);

        User user = new User();
        user.setEnabled(false);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        service.confirmToken(token);

        assertTrue(user.isEnabled());
        verify(userRepository).save(user);
    }

    // resendConfirmationEmail
    @Test
    void resendConfirmation_userNotFound_throws() {
        when(userRepository.findByEmail("x@x")).thenReturn(Optional.empty());
        assertThrows(InvalidTokenException.class,
                () -> service.resendConfirmationEmail("x@x")
        );
    }

    @Test
    void resendConfirmation_alreadyConfirmed_throws() {
        User user = new User();
        user.setEnabled(true);
        when(userRepository.findByEmail("e@e")).thenReturn(Optional.of(user));

        assertThrows(AlreadyConfirmedException.class,
                () -> service.resendConfirmationEmail("e@e")
        );
    }

    @Test
    void resendConfirmation_success_sendsEmail() {
        String email = "foo@bar";
        User user = new User();
        user.setEnabled(false);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        when(jwtService.generateToken(email)).thenReturn("new-token");

        service.resendConfirmationEmail(email);

        verify(emailService).sendRegistrationEmail(email, "new-token");
    }
}
