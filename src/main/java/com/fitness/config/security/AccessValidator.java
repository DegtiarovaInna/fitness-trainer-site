package com.fitness.config.security;

import com.fitness.repositories.StudioRepository;
import com.fitness.services.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessValidator {

    private final AuthServiceImpl authServiceImpl;
    private final StudioRepository studioRepository;

    public boolean isCurrentUser(Long userId) {
        return userId != null && userId.equals(authServiceImpl.getCurrentUserId());
    }

    public boolean isCurrentStudio(Long studioId) {
        return studioRepository.findById(studioId)
                .map(studio -> studio.getAdmin() != null && studio.getAdmin().getId().equals(authServiceImpl.getCurrentUserId()))
                .orElse(false);
    }


}
