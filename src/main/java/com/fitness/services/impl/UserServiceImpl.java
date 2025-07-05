package com.fitness.services.impl;

import com.fitness.config.security.AccessValidator;
import com.fitness.dto.ChangePasswordRequest;
import com.fitness.dto.RegisterUserRequest;
import com.fitness.dto.UpdateUserRequest;
import com.fitness.dto.UserDTO;
import com.fitness.enums.Role;
import com.fitness.exceptions.UserNotFoundException;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.mappers.UserMapper;
import com.fitness.models.User;
import com.fitness.repositories.UserRepository;
import com.fitness.services.interfaces.AuthService;
import com.fitness.services.interfaces.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.fitness.services.interfaces.SecurityService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthService authService;
  //  private final AccessValidator accessValidator;
    private final SecurityService securityService;
    private final PasswordEncoder passwordEncoder;


    @Override
        // Регистрация нового пользователя
        public UserDTO registerUser(RegisterUserRequest registerUserRequest) {
            // Создаем нового пользователя
            User user = new User();
            user.setName(registerUserRequest.getName());
            user.setEmail(registerUserRequest.getEmail());
            user.setPhoneNumber(registerUserRequest.getPhoneNumber());
            user.setRole(Role.USER); // По умолчанию роль USER
            user.setPassword(passwordEncoder.encode(registerUserRequest.getPassword()));

            user = userRepository.save(user);

            // Возвращаем DTO
            return userMapper.userToUserDTO(user);
        }
    @Override
        // Получение пользователя по ID
        public UserDTO getUser(Long id) {
//            Role currentRole = authService.getCurrentUserRole();
//
//            if (currentRole == Role.USER || currentRole == Role.USER_PRO) {
//                // проверяем, что id совпадает с текущим пользователем
//                if (!accessValidator.isCurrentUser(id)) {
//                    throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED);
//                }
//            }
        securityService.requireSelfOrAdminOrDev(id);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND));
            return userMapper.userToUserDTO(user);
        }
    @Override

    public List<UserDTO> getAllUsers() {
        securityService.requireAdminOrDev();
        return userRepository.findAll().stream()
                .map(userMapper::userToUserDTO)
                .collect(Collectors.toList());
    }
    @Override
    public UserDTO updateUser(Long id, UpdateUserRequest dto) {
//        Role currentRole = authService.getCurrentUserRole();
//
//        if (currentRole == Role.USER || currentRole == Role.USER_PRO) {
//            if (!accessValidator.isCurrentUser(id)) {
//                throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED);
//            }
//        }
        securityService.requireSelfOrAdminOrDev(id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND));

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());

        // 5) Сохраняем и возвращаем DTO
        User saved = userRepository.save(user);
        return userMapper.userToUserDTO(saved);
    }
    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest req) {
        // 1) Права: свой профиль или админ/DEV
//        Role role = authService.getCurrentUserRole();
//        Long me = authService.getCurrentUserId();
//        if ((role == Role.USER || role == Role.USER_PRO) && !me.equals(id)) {
//            // простые пользователи могут менять только свой пароль
//            throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED);
//        }
        securityService.requireSelfOrAdminOrDev(id);
        // 2) Загружаем пользователя и проверяем существование
        User u = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND));

        // 3) Проверяем, что текущий пароль совпадает
        if (!passwordEncoder.matches(req.getCurrentPassword(), u.getPassword())) {
            throw new BadCredentialsException("Неверный текущий пароль");
        }

        // 4) Проверяем, что новый пароль и его подтверждение совпадают
        if (!req.getNewPassword().equals(req.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        // 5) Хешируем новый пароль и сохраняем
        u.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(u);
    }
    @Override
    public void deleteUser(Long id) {
//        Role currentRole = authService.getCurrentUserRole();
//        Long currentUserId = authService.getCurrentUserId();
//
//        // Только ADMIN/DEV или сам пользователь
//        if (currentRole != Role.ADMIN && currentRole != Role.DEV && !currentUserId.equals(id)) {
//            throw new AccessDeniedException(ErrorMessage.ACCESS_DENIED_SELF_ONLY);
//        }
        securityService.requireSelfOrAdminOrDev(id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND));
        userRepository.delete(user);
    }
    }


