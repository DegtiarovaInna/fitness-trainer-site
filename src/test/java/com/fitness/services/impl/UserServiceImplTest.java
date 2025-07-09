package com.fitness.services.impl;
import com.fitness.config.security.JwtService;
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
import com.fitness.services.interfaces.CurrentUserService;
import com.fitness.services.interfaces.EmailService;
import com.fitness.services.interfaces.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class UserServiceImplTest {
    private UserRepository userRepo;
    private UserMapper userMapper;
    private CurrentUserService currentUserService;
    private SecurityService securityService;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private EmailService emailService;
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepository.class);
        userMapper = mock(UserMapper.class);
        currentUserService = mock(CurrentUserService.class);
        securityService = mock(SecurityService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        emailService = mock(EmailService.class);
        service = new UserServiceImpl(
                userRepo,
                userMapper,
                currentUserService,
                securityService,
                passwordEncoder,
                jwtService,
                emailService
        );
    }

    // registerUser
    @Test
    void registerUser_savesAndSendsEmail_returnsDto() {
        var req = new RegisterUserRequest();
        req.setName("Alice");
        req.setEmail("alice@example.com");
        req.setPhoneNumber("+1234567890");
        req.setPassword("pass1234");

        when(passwordEncoder.encode("pass1234")).thenReturn("encodedPass");
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Alice");
        savedUser.setEmail("alice@example.com");
        savedUser.setPhoneNumber("+1234567890");
        savedUser.setRole(Role.USER);
        savedUser.setPassword("encodedPass");
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken("alice@example.com")).thenReturn("jwtToken");
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        when(userMapper.userToUserDTO(savedUser)).thenReturn(dto);

        UserDTO result = service.registerUser(req);

        verify(passwordEncoder).encode("pass1234");
        verify(userRepo).save(any(User.class));
        verify(jwtService).generateToken("alice@example.com");
        verify(emailService).sendRegistrationEmail("alice@example.com", "jwtToken");
        assertSame(dto, result);
    }

    // getUser
    @Test
    void getUser_existing_returnsDto() {
        when(userRepo.findById(2L)).thenReturn(Optional.of(new User()));
        when(userMapper.userToUserDTO(any())).thenReturn(new UserDTO());

        service.getUser(2L);
        verify(securityService).requireSelfOrAdminOrDev(2L);
    }

    @Test
    void getUser_missing_throws() {
        when(userRepo.findById(3L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.getUser(3L));
    }

    // getAllUsers
    @Test
    void getAllUsers_maps() {
        User u1 = new User(); u1.setId(1L);
        User u2 = new User(); u2.setId(2L);
        when(userRepo.findAll()).thenReturn(List.of(u1, u2));
        when(userMapper.userToUserDTO(u1)).thenReturn(new UserDTO());
        when(userMapper.userToUserDTO(u2)).thenReturn(new UserDTO());

        service.getAllUsers();
        verify(securityService).requireAdminOrDev();
    }

    // updateUser
    @Test
    void updateUser_existing_returnsDto() {
        var dto = new UpdateUserRequest();
        dto.setName("Bob");
        dto.setEmail("bob@example.com");
        dto.setPhoneNumber("+0987654321");
        User user = new User(); user.setId(4L);
        when(userRepo.findById(4L)).thenReturn(Optional.of(user));
        User saved = new User(); saved.setId(4L);
        when(userRepo.save(user)).thenReturn(saved);
        when(userMapper.userToUserDTO(saved)).thenReturn(new UserDTO());

        var result = service.updateUser(4L, dto);

        verify(securityService).requireSelfOrAdminOrDev(4L);
        assertNotNull(result);
    }

    @Test
    void updateUser_missing_throws() {
        when(userRepo.findById(5L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.updateUser(5L, new UpdateUserRequest()));
    }

    // changePassword
    @Test
    void changePassword_wrongCurrent_throws() {
        when(userRepo.findById(6L)).thenReturn(Optional.of(new User()));
        when(passwordEncoder.matches("old","encoded")).thenReturn(false);
        var req = new ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("newPass1");
        req.setNewPasswordConfirm("newPass1");
        assertThrows(BadCredentialsException.class, () -> service.changePassword(6L, req));
    }

    @Test
    void changePassword_mismatchNew_throws() {
        User u = new User(); u.setPassword("encoded");
        when(userRepo.findById(7L)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("old","encoded")).thenReturn(true);
        var req = new ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("new1");
        req.setNewPasswordConfirm("new2");
        assertThrows(IllegalArgumentException.class, () -> service.changePassword(7L, req));
    }

    @Test
    void changePassword_success_setsNew() {
        User u = new User(); u.setPassword("encodedOld");
        when(userRepo.findById(8L)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("old","encodedOld")).thenReturn(true);
        var req = new ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("newPass");
        req.setNewPasswordConfirm("newPass");
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");

        service.changePassword(8L, req);

        verify(passwordEncoder).encode("newPass");
        verify(userRepo).save(u);
        assertEquals("encodedNew", u.getPassword());
    }

    // deleteUser
    @Test
    void deleteUser_existing_deletes() {
        User u = new User(); u.setId(9L);
        when(userRepo.findById(9L)).thenReturn(Optional.of(u));
        service.deleteUser(9L);
        verify(securityService).requireSelfOrAdminOrDev(9L);
        verify(userRepo).delete(u);
    }

    @Test
    void deleteUser_missing_throws() {
        when(userRepo.findById(10L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.deleteUser(10L));
    }
}
