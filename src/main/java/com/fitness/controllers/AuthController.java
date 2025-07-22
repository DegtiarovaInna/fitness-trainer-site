package com.fitness.controllers;

import com.fitness.config.security.JwtService;
import com.fitness.dto.AuthResponse;
import com.fitness.dto.RegisterUserRequest;
import com.fitness.dto.UserDTO;
import com.fitness.services.interfaces.AuthenticationService;
import com.fitness.services.interfaces.ConfirmationService;
import com.fitness.services.interfaces.PasswordResetService;
import com.fitness.services.interfaces.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final ConfirmationService confirmationService;
   private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        UserDTO userDTO = userService.registerUser(registerUserRequest);
        return ResponseEntity.ok(userDTO);
    }
    @RateLimiter(name = "loginRateLimiter")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
           @RequestParam String email,
          @RequestParam String password
   ) {
              return ResponseEntity.ok(authenticationService.login(email, password));
           }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
          @RequestParam String refreshToken
  ) {
              return ResponseEntity.ok(authenticationService.refresh(refreshToken));
          }
    @GetMapping("/confirm")
    public ResponseEntity<String> confirmEmail(@RequestParam String token) {
        confirmationService.confirmToken(token);
              return ResponseEntity.ok("Email confirmed. You can now login.");
    }

    @PostMapping("/resend")
    public ResponseEntity<Void> resendConfirmation(@RequestParam String email) {
        confirmationService.resendConfirmationEmail(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset/request")
   public ResponseEntity<Void> requestReset(@RequestParam String email) {
               passwordResetService.requestReset(email);
               return ResponseEntity.noContent().build();
           }

          @PostMapping("/resetPassword")
   public ResponseEntity<Void> resetPassword(
           @RequestParam String token,
           @RequestParam String newPassword
   ) {
               passwordResetService.resetPassword(token, newPassword);
               return ResponseEntity.noContent().build();
           }

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
