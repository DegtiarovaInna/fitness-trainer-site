package com.fitness.services.impl;

import com.fitness.config.security.UserDetailsImpl;
import com.fitness.enums.Role;
import com.fitness.exceptions.UserNotAuthenticatedException;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements com.fitness.services.interfaces.AuthService {
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails.getUser();
        }
        throw new UserNotAuthenticatedException(ErrorMessage.USER_NOT_AUTHENTICATED);
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }
}
