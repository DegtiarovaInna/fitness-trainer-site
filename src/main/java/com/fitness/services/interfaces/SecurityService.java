package com.fitness.services.interfaces;

public interface SecurityService {
    void requireAdminOrDev();
    void requireSelfOrAdminOrDev(Long userId);
    void requireStudioOwnerOrAdminOrDev(Long studioId);
}
