package com.fitness.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.config.security.JwtService;
import com.fitness.config.security.UserDetailsServiceImpl;
import com.fitness.dto.AuthResponse;
import com.fitness.dto.RegisterUserRequest;
import com.fitness.dto.UserDTO;
import com.fitness.exceptions.AlreadyConfirmedException;
import com.fitness.exceptions.InvalidTokenException;
import com.fitness.exceptions.RefreshTokenException;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.services.interfaces.AuthenticationService;
import com.fitness.services.interfaces.ConfirmationService;
import com.fitness.services.interfaces.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @MockBean
    private ConfirmationService confirmationService;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("POST /auth/register — успешная регистрация")
    void registerUser_success() throws Exception {
        var req = new RegisterUserRequest();
        req.setName("Inna");
        req.setEmail("inna@example.com");
        req.setPhoneNumber("+79161234567");
        req.setPassword("secret123");

        var dto = new UserDTO();
        dto.setId(42L);
        dto.setName("Inna");
        dto.setEmail("inna@example.com");
        dto.setPhoneNumber("+79161234567");

        when(userService.registerUser(eq(req))).thenReturn(dto);

        mvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.name").value("Inna"))
                .andExpect(jsonPath("$.email").value("inna@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+79161234567"));

        verify(userService).registerUser(eq(req));
    }

    @Test
    @DisplayName("POST /auth/login — успешный вход")
    void login_success() throws Exception {
        String email = "inna@example.com";
        String pass  = "secret";
        var auth = new AuthResponse("atoken", "rtoken");

        when(authenticationService.login(email, pass)).thenReturn(auth);

        mvc.perform(post("/auth/login")
                        .param("email", email)
                        .param("password", pass))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("atoken"))
                .andExpect(jsonPath("$.refreshToken").value("rtoken"));

        verify(authenticationService).login(email, pass);
    }

    @Test
    @DisplayName("POST /auth/refresh — успешный refresh")
    void refresh_success() throws Exception {
        String rtoken = "rtoken";
        var auth     = new AuthResponse("newAccess", rtoken);

        when(authenticationService.refresh(rtoken)).thenReturn(auth);

        mvc.perform(post("/auth/refresh")
                        .param("refreshToken", rtoken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccess"))
                .andExpect(jsonPath("$.refreshToken").value(rtoken));

        verify(authenticationService).refresh(rtoken);
    }

    @Test
    @DisplayName("GET /auth/confirm — подтверждение email")
    void confirmEmail_success() throws Exception {
        String token = "sometoken";

        mvc.perform(get("/auth/confirm")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Email confirmed. You can now login."));

        verify(confirmationService).confirmToken(token);
    }

    @Test
    @DisplayName("POST /auth/resend — повторно выслать подтверждение")
    void resendConfirmation_success() throws Exception {
        String email = "inna@example.com";

        mvc.perform(post("/auth/resend")
                        .param("email", email))
                .andExpect(status().isNoContent());

        verify(confirmationService).resendConfirmationEmail(email);
    }

    @Test @DisplayName("POST /auth/register — неверный пароль возвращает 400")
    void registerUser_validationError() throws Exception {
        var req = new RegisterUserRequest();
        req.setName("Inna");
        req.setEmail("inna@example.com");
        req.setPhoneNumber("+79161234567");
        req.setPassword("nopunctuation");  // нет цифр
        mvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password")
                        .value("The password must contain letters and numbers"));
    }
    @Test
    @DisplayName("POST /auth/register — дублирование email → 400 EMAIL_ALREADY_EXISTS")
    void registerUser_emailExists() throws Exception {
        var req = new RegisterUserRequest();
        req.setName("Inna");
        req.setEmail("inna@example.com");
        req.setPhoneNumber("+79161234567");
        req.setPassword("secret1234");

        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(userService).registerUser(any());

        mvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message")
                        .value(ErrorMessage.USER_EMAIL_ALREADY_EXISTS));
    }
    @Test
    @DisplayName("POST /auth/login — плохие креды → 401 INVALID_CREDENTIALS")
    void login_badCredentials() throws Exception {
        doThrow(new BadCredentialsException("bad"))
                .when(authenticationService).login(anyString(), anyString());

        mvc.perform(post("/auth/login")
                        .param("email", "x@y.z")
                        .param("password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));
    }
    @Test
    @DisplayName("POST /auth/refresh — неверный refresh → 401 INVALID_REFRESH_TOKEN")
    void refresh_invalidToken() throws Exception {
        doThrow(new RefreshTokenException("nope"))
                .when(authenticationService).refresh(anyString());

        mvc.perform(post("/auth/refresh")
                        .param("refreshToken", "bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_REFRESH_TOKEN"))
                .andExpect(jsonPath("$.message").value("nope"));
    }
    @Test
    @DisplayName("GET /auth/confirm — токен неверный → 401 INVALID_TOKEN")
    void confirmEmail_invalidToken() throws Exception {
        doThrow(new InvalidTokenException("bad"))
                .when(confirmationService).confirmToken(anyString());

        mvc.perform(get("/auth/confirm")
                        .param("token", "bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_TOKEN"))
                .andExpect(jsonPath("$.message").value("bad"));
    }

    @Test
    @DisplayName("GET /auth/confirm — уже подтверждён → 200 ALREADY_CONFIRMED")
    void confirmEmail_already() throws Exception {
        doThrow(new AlreadyConfirmedException("been here"))
                .when(confirmationService).confirmToken(anyString());

        mvc.perform(get("/auth/confirm")
                        .param("token", "still"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("ALREADY_CONFIRMED"))
                .andExpect(jsonPath("$.message").value("been here"));
    }

}
