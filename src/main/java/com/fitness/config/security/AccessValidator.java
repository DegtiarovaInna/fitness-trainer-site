package com.fitness.config.security;

import com.fitness.repositories.StudioRepository;
import com.fitness.services.impl.CurrentUserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessValidator {

    private final CurrentUserServiceImpl currentUserServiceImpl;
    private final StudioRepository studioRepository;

    public boolean isCurrentUser(Long userId) {
        return userId != null && userId.equals(currentUserServiceImpl.getCurrentUserId());
    }

    public boolean isCurrentStudio(Long studioId) {
        return studioRepository.findById(studioId)
                .map(studio -> studio.getAdmin() != null && studio.getAdmin().getId().equals(currentUserServiceImpl.getCurrentUserId()))
                .orElse(false);
    }


}
