package com.fitness.services.impl;

import com.fitness.enums.Role;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.services.interfaces.SecurityService;
import com.fitness.services.interfaces.CurrentUserService;
import com.fitness.config.security.AccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {
    private final CurrentUserService currentUserService;
    private final AccessValidator accessValidator;

    @Override
    public void requireAdminOrDev() {
        Role role = currentUserService.getCurrentUserRole();
        if (role != Role.ADMIN && role != Role.DEV) {
            throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED);
        }
    }

    @Override
    public void requireSelfOrAdminOrDev(Long userId) {
        Role role = currentUserService.getCurrentUserRole();
        Long me = currentUserService.getCurrentUserId();
        if ((role == Role.USER || role == Role.USER_PRO) && !me.equals(userId)) {
            throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED);
        }
    }

    @Override
    public void requireStudioOwnerOrAdminOrDev(Long studioId) {
        Role role = currentUserService.getCurrentUserRole();
        if (role == Role.USER_PRO && !accessValidator.isCurrentStudio(studioId)) {
            throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED_NOT_YOUR_STUDIO);
        }
    }
}
