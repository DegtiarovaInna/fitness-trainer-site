package com.fitness.services.interfaces;

import com.fitness.dto.ChangePasswordRequest;
import com.fitness.dto.RegisterUserRequest;
import com.fitness.dto.UpdateUserRequest;
import com.fitness.dto.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO registerUser(RegisterUserRequest registerUserRequest);
    UserDTO getUser(Long id);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(Long id, UpdateUserRequest dto);
    void changePassword(Long id, ChangePasswordRequest req);
    void deleteUser(Long id);
}
