package com.fitness.controllers;

import com.fitness.dto.AuthResponse;
import com.fitness.dto.RegisterUserRequest;
import com.fitness.dto.UserDTO;
import com.fitness.services.interfaces.AuthenticationService;
import com.fitness.services.interfaces.ConfirmationService;
import com.fitness.services.interfaces.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final ConfirmationService confirmationService;
   private final AuthenticationService authenticationService;


    @PostMapping("/register")
    @PermitAll
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
    @PermitAll
    public ResponseEntity<String> confirmEmail(@RequestParam String token) {
        confirmationService.confirmToken(token);
              return ResponseEntity.ok("Email confirmed. You can now login.");
    }
    @PostMapping("/resend")
    @PermitAll
    public ResponseEntity<Void> resendConfirmation(@RequestParam String email) {
        confirmationService.resendConfirmationEmail(email);
        return ResponseEntity.noContent().build();
    }
}
