package com.fitness.services.impl;
import com.fitness.config.security.AccessValidator;
import com.fitness.enums.Role;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.services.interfaces.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class SecurityServiceImplTest {
    private CurrentUserService currentUserService;
    private AccessValidator accessValidator;
    private SecurityServiceImpl service;

    @BeforeEach
    void setUp() {
        currentUserService = mock(CurrentUserService.class);
        accessValidator    = mock(AccessValidator.class);
        service = new SecurityServiceImpl(currentUserService, accessValidator);
    }

    // requireAdminOrDev()
    @Test
    void requireAdminOrDev_whenAdmin_noThrow() {
        when(currentUserService.getCurrentUserRole()).thenReturn(Role.ADMIN);
        assertDoesNotThrow(() -> service.requireAdminOrDev());
    }

    @Test
    void requireAdminOrDev_whenDev_noThrow() {
        when(currentUserService.getCurrentUserRole()).thenReturn(Role.DEV);
        assertDoesNotThrow(() -> service.requireAdminOrDev());
    }

    @Test
    void requireAdminOrDev_whenUser_throws() {
        when(currentUserService.getCurrentUserRole()).thenReturn(Role.USER);
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> service.requireAdminOrDev()
        );
        assertEquals(ErrorMessage.ACCESS_DENIED, ex.getMessage());
    }

    // requireSelfOrAdminOrDev()
    @Test
    void requireSelfOrAdminOrDev_selfUser_noThrow() {
        when(currentUserService.getCurrentUserRole()).thenReturn(Role.USER);
        when(currentUserService.getCurrentUserId()).thenReturn(5L);
        assertDoesNotThrow(() -> service.requireSelfOrAdminOrDev(5L));
    }

    @Test
    void requireSelfOrAdminOrDev_userMismatch_throws() {
        when(currentUserService.getCurrentUserRole()).thenReturn(Role.USER_PRO);
        when(currentUserService.getCurrentUserId()).thenReturn(5L);
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> service.requireSelfOrAdminOrDev(9L)
        );
        assertEquals(ErrorMessage.ACCESS_DENIED, ex.getMessage());
    }

    @Test
    void requireSelfOrAdminOrDev_whenDev_noThrow() {
        when(currentUserService.getCurrentUserRole()).thenReturn(Role.DEV);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        assertDoesNotThrow(() -> service.requireSelfOrAdminOrDev(999L));
    }

    // requireStudioOwnerOrAdminOrDev()
    @Test
    void requireStudioOwnerOrAdminOrDev_admin_noThrow() {
        when(currentUserService.getCurrentUserRole()).thenReturn(Role.ADMIN);
        assertDoesNotThrow(() -> service.requireStudioOwnerOrAdminOrDev(1L));
    }

    @Test
    void requireStudioOwnerOrAdminOrDev_dev_noThrow() {
        when(currentUserService.getCurrentUserRole()).thenReturn(Role.DEV);
        assertDoesNotThrow(() -> service.requireStudioOwnerOrAdminOrDev(2L));
    }

    @Test
    void requireStudioOwnerOrAdminOrDev_user_noThrow() {
        when(currentUserService.getCurrentUserRole()).thenReturn(Role.USER);
        assertDoesNotThrow(() -> service.requireStudioOwnerOrAdminOrDev(3L));
    }

    @Test
    void requireStudioOwnerOrAdminOrDev_userProOwner_noThrow() {
        when(currentUserService.getCurrentUserRole()).thenReturn(Role.USER_PRO);
        when(accessValidator.isCurrentStudio(5L)).thenReturn(true);
        assertDoesNotThrow(() -> service.requireStudioOwnerOrAdminOrDev(5L));
    }

    @Test
    void requireStudioOwnerOrAdminOrDev_userProNotOwner_throws() {
        when(currentUserService.getCurrentUserRole()).thenReturn(Role.USER_PRO);
        when(accessValidator.isCurrentStudio(7L)).thenReturn(false);
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> service.requireStudioOwnerOrAdminOrDev(7L)
        );
        assertEquals(ErrorMessage.ACCESS_DENIED_NOT_YOUR_STUDIO, ex.getMessage());
    }
}
