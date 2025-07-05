package com.fitness.services.interfaces;

import com.fitness.enums.Role;
import com.fitness.models.User;

public interface AuthService {
    User getCurrentUser();
    Long getCurrentUserId();
    Role getCurrentUserRole();
}
