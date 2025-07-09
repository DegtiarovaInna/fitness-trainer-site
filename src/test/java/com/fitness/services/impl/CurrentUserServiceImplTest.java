package com.fitness.services.impl;
import com.fitness.config.security.UserDetailsImpl;
import com.fitness.enums.Role;
import com.fitness.exceptions.UserNotAuthenticatedException;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.models.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class CurrentUserServiceImplTest {
    private CurrentUserServiceImpl service;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        originalContext = SecurityContextHolder.getContext();
        SecurityContextHolder.clearContext();
        service = new CurrentUserServiceImpl();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalContext);
    }

    @Test
    void getCurrentUser_authenticated_returnsUser() {
        User user = new User();
        user.setId(123L);
        user.setRole(Role.USER_PRO);

        UserDetailsImpl ud = new UserDetailsImpl(user);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(ud);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertSame(user, service.getCurrentUser());
        assertEquals(123L,  service.getCurrentUserId());
        assertEquals(Role.USER_PRO, service.getCurrentUserRole());
    }

    @Test
    void getCurrentUser_noAuthentication_throws() {
        assertThrows(UserNotAuthenticatedException.class,
                () -> service.getCurrentUser(),
                "Expected USER_NOT_AUTHENTICATED"
        );
    }

    @Test
    void getCurrentUser_wrongPrincipalType_throws() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(new Object());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(UserNotAuthenticatedException.class,
                () -> service.getCurrentUser()
        );
    }
}
