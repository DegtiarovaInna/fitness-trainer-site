package com.fitness.services.interfaces;

public interface SecurityService {
    /** Только ADMIN или DEV */
    void requireAdminOrDev();
    /** Только свой профиль или ADMIN/DEV */
    void requireSelfOrAdminOrDev(Long userId);
    /** Только админ своей студии (USER_PRO for-owner) или ADMIN/DEV */
    void requireStudioOwnerOrAdminOrDev(Long studioId);
}
