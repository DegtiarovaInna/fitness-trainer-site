package com.fitness.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.config.security.JwtService;
import com.fitness.config.security.UserDetailsServiceImpl;
import com.fitness.dto.ChangePasswordRequest;
import com.fitness.dto.UpdateUserRequest;
import com.fitness.dto.UserDTO;
import com.fitness.exceptions.UserNotFoundException;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.services.interfaces.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;


    @Test
    @DisplayName("GET /api/users/{id} — успешное получение пользователя")
    void getUser_success() throws Exception {
        var dto = new UserDTO();
        dto.setId(1L);
        dto.setName("Inna");
        dto.setEmail("inna@example.com");
        dto.setPhoneNumber("+79161234567");

        when(userService.getUser(1L)).thenReturn(dto);

        mvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Inna"))
                .andExpect(jsonPath("$.email").value("inna@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+79161234567"));
    }

    @Test
    @DisplayName("GET /api/users/{id} — пользователь не найден")
    void getUser_notFound() throws Exception {
        when(userService.getUser(99L))
                .thenThrow(new UserNotFoundException(ErrorMessage.USER_NOT_FOUND));

        mvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ErrorMessage.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("GET /api/users — успешное получение списка пользователей")
    void getAllUsers_success() throws Exception {
        var dto1 = new UserDTO();
        dto1.setId(1L);
        dto1.setName("Inna");
        dto1.setEmail("a@b.com");
        dto1.setPhoneNumber("p1");

        var dto2 = new UserDTO();
        dto2.setId(2L);
        dto2.setName("Bob");
        dto2.setEmail("b@c.com");
        dto2.setPhoneNumber("p2");

        when(userService.getAllUsers()).thenReturn(List.of(dto1, dto2));

        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("PUT /api/users/{id} — успешное обновление пользователя")
    void updateUser_success() throws Exception {
        var req = new UpdateUserRequest();
        req.setName("NewName");
        req.setEmail("new@example.com");
        req.setPhoneNumber("+70000000000");

        var dto = new UserDTO();
        dto.setId(1L);
        dto.setName("NewName");
        dto.setEmail("new@example.com");
        dto.setPhoneNumber("+70000000000");

        when(userService.updateUser(eq(1L), eq(req))).thenReturn(dto);

        mvc.perform(put("/api/users/1")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("NewName"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+70000000000"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} — пользователь не найден при обновлении")
    void updateUser_notFound() throws Exception {
        var req = new UpdateUserRequest();
        req.setName("Xsdf");
        req.setEmail("xytyu@gmail.com");
        req.setPhoneNumber("+38000000000");

        doThrow(new UserNotFoundException(ErrorMessage.USER_NOT_FOUND))
                .when(userService).updateUser(eq(5L), any());

        mvc.perform(put("/api/users/5")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ErrorMessage.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} — успешное удаление пользователя")
    void deleteUser_success() throws Exception {
        mvc.perform(delete("/api/users/3"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} — пользователь не найден при удалении")
    void deleteUser_notFound() throws Exception {
        doThrow(new UserNotFoundException(ErrorMessage.USER_NOT_FOUND))
                .when(userService).deleteUser(7L);

        mvc.perform(delete("/api/users/7"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ErrorMessage.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("PUT /api/users/{id}/password — успешная смена пароля")
    void changePassword_success() throws Exception {
        var req = new ChangePasswordRequest();
        req.setCurrentPassword("old12345678");
        req.setNewPassword("new12345678");
        req.setNewPasswordConfirm("new12345678");

        mvc.perform(put("/api/users/2/password")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /api/users/{id}/password — неверный текущий пароль")
    void changePassword_invalidCurrent() throws Exception {
        var req = new ChangePasswordRequest();
        req.setCurrentPassword("wrong1234");
        req.setNewPassword("n12345678");
        req.setNewPasswordConfirm("n12345678");

        doThrow(new BadCredentialsException(ErrorMessage.INVALID_CURRENT_PASSWORD))
                .when(userService).changePassword(eq(2L), any());

        mvc.perform(put("/api/users/2/password")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("PUT /api/users/{id}/password — новые пароли не совпадают")
    void changePassword_mismatch() throws Exception {
        var req = new ChangePasswordRequest();
        req.setCurrentPassword("old111222");
        req.setNewPassword("aaaa11111");
        req.setNewPasswordConfirm("bbbb11111");

        doThrow(new IllegalArgumentException(ErrorMessage.PASSWORDS_DO_NOT_MATCH))
                .when(userService).changePassword(eq(2L), any());

        mvc.perform(put("/api/users/2/password")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("An unexpected error occurred. Please try again later."));
    }
}
