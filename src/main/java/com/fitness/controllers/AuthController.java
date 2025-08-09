package com.fitness.controllers;

import com.fitness.config.security.JwtService;
import com.fitness.dto.AuthResponse;
import com.fitness.dto.RegisterUserRequest;
import com.fitness.dto.UserDTO;
import com.fitness.exceptions.RefreshTokenException;
import com.fitness.services.interfaces.AuthenticationService;
import com.fitness.services.interfaces.ConfirmationService;
import com.fitness.services.interfaces.PasswordResetService;
import com.fitness.services.interfaces.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

@Tag(name = "Auth", description = "Authentication & account lifecycle")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.sameSite:Lax}")
    private String cookieSameSite;

    private final UserService userService;
    private final ConfirmationService confirmationService;
    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Operation(summary = "Register new user",
            description = "Creates a user and sends a confirmation email (in prod).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation or business error",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Email already exists",
                    content = @Content)
    })
    @RateLimiter(name = "registerRateLimiter")
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        UserDTO userDTO = userService.registerUser(registerUserRequest);
        return ResponseEntity.ok(userDTO);
    }

    @Operation(
            summary = "Login",
            description = "Returns accessToken in body and sets refreshToken in HttpOnly cookie"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successful authorization",
            content = @Content(schema = @Schema(implementation = AuthResponse.class),
                    examples = @ExampleObject(value = "{\"accessToken\":\"<JWT>\",\"refreshToken\":null}"))
    )
    @RateLimiter(name = "loginRateLimiter")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestParam String email,
            @RequestParam String password
    ) {
        AuthResponse auth = authenticationService.login(email, password);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", auth.getRefreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(60L * 60 * 24 * 30)
                .build();

        AuthResponse body = new AuthResponse(auth.getAccessToken(), null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(body);
    }

    @Operation(summary = "Refresh tokens via HttpOnly cookie")
    @ApiResponse(
            responseCode = "200",
            description = "Tokens updated, access in body, refresh in Set-Cookie",
            content = @Content(schema = @Schema(implementation = AuthResponse.class),
                    examples = @ExampleObject(value = "{\"accessToken\":\"<NEW_JWT>\",\"refreshToken\":null}"))
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshCookie
    ) {
        if (refreshCookie == null || refreshCookie.isBlank()) {
            throw new RefreshTokenException("Refresh cookie is missing");
        }
        AuthResponse auth = authenticationService.refresh(refreshCookie);

        ResponseCookie newCookie = ResponseCookie.from("refreshToken", auth.getRefreshToken())
                .httpOnly(true).secure(cookieSecure)
                .sameSite(cookieSameSite).path("/").maxAge(60L * 60 * 24 * 30).build();

        AuthResponse body = new AuthResponse(auth.getAccessToken(), null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newCookie.toString())
                .body(body);
    }

    @Operation(summary = "Confirm email", description = "Confirms email by token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email confirmed"),
            @ApiResponse(responseCode = "400", description = "Invalid token", content = @Content)
    })
    @GetMapping("/confirm")
    public ResponseEntity<String> confirmEmail(@RequestParam String token) {
        confirmationService.confirmToken(token);
        return ResponseEntity.ok("Email confirmed. You can now login.");
    }

    @Operation(summary = "Resend confirmation email",
            description = "Sends a new confirmation email to the provided address.")
    @ApiResponse(responseCode = "204", description = "Sent")
    @PostMapping("/resend")
    public ResponseEntity<Void> resendConfirmation(@RequestParam String email) {
        confirmationService.resendConfirmationEmail(email);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Request password reset",
            description = "Sends password reset link/token to the email.")
    @ApiResponse(responseCode = "204", description = "Sent")
    @PostMapping("/reset/request")
    public ResponseEntity<Void> requestReset(@RequestParam String email) {
        passwordResetService.requestReset(email);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reset password by token",
            description = "Verifies reset token and updates password.")
    @ApiResponse(responseCode = "204", description = "Password updated")
    @PostMapping("/resetPassword")
    public ResponseEntity<Void> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword
    ) {
        passwordResetService.resetPassword(token, newPassword);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Check reset token", description = "Validates password reset token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "400", description = "Invalid token", content = @Content)
    })
    @GetMapping("/reset")
    public ResponseEntity<Void> checkResetToken(@RequestParam String token) {

        String email;
        try {
            email = jwtService.extractUsername(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        UserDetails ud = userDetailsService.loadUserByUsername(email);
        if (!jwtService.isResetToken(token, ud)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok().build();
    }
}
