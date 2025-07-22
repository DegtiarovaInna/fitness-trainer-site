package com.fitness.services.impl;

import com.fitness.config.security.JwtService;
import com.fitness.enums.BookingStatus;
import com.fitness.exceptions.UserHasActiveBookings;
import com.fitness.repositories.BookingRepository;
import com.fitness.services.interfaces.EmailService;
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
import com.fitness.services.interfaces.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.fitness.services.interfaces.SecurityService;
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
    private final SecurityService securityService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final BookingRepository bookingRepository;


    @Override

        public UserDTO registerUser(RegisterUserRequest registerUserRequest) {

            User user = new User();
            user.setName(registerUserRequest.getName());
            user.setEmail(registerUserRequest.getEmail());
            user.setPhoneNumber(registerUserRequest.getPhoneNumber());
            user.setRole(Role.USER);
            user.setPassword(passwordEncoder.encode(registerUserRequest.getPassword()));

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getEmail());

        emailService.sendRegistrationEmail(
                savedUser.getEmail(),
                token

        );
            return userMapper.userToUserDTO(savedUser);
        }
    @Override
        public UserDTO getUser(Long id) {
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
        securityService.requireSelfOrAdminOrDev(id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND));
        boolean changed = false;

        if (!user.getName().equals(dto.getName())) {
            user.setName(dto.getName());
            changed = true;
        }
        if (!user.getEmail().equals(dto.getEmail())) {
            user.setEmail(dto.getEmail());
            changed = true;
        }
        if (!user.getPhoneNumber().equals(dto.getPhoneNumber())) {
            user.setPhoneNumber(dto.getPhoneNumber());
            changed = true;
        }

        User saved = userRepository.save(user);
        if (changed) {
            emailService.sendProfileUpdateEmail(saved.getEmail());
        }
        return userMapper.userToUserDTO(saved);
    }
    @Override
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest req) {

        securityService.requireSelfOrAdminOrDev(id);
        User u = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND));

        if (!passwordEncoder.matches(req.getCurrentPassword(), u.getPassword())) {
            throw new BadCredentialsException(ErrorMessage.INVALID_CURRENT_PASSWORD);
        }

        if (!req.getNewPassword().equals(req.getNewPasswordConfirm())) {
            throw new IllegalArgumentException(ErrorMessage.PASSWORDS_DO_NOT_MATCH);
        }

        u.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(u);
        emailService.sendPasswordChangedEmail(u.getEmail());
    }
    @Override
    public void deleteUser(Long id) {
        securityService.requireSelfOrAdminOrDev(id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND));

        boolean hasActive = bookingRepository.existsByUserIdAndStatusNot(
                user.getId(), BookingStatus.CANCELLED);
        if (hasActive) {
            throw new UserHasActiveBookings(ErrorMessage.USER_HAS_ACTIVE_BOOKINGS);
        }

        userRepository.delete(user);
        emailService.sendGoodbyeEmail(user.getEmail());
    }
    }


