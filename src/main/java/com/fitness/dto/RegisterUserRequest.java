package com.fitness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserRequest {
    @NotBlank(message = "Name is required")
    @Schema(description = "Username", example = "Alex Frolow")
    private String name;
    @NotBlank(message = "Email is required")
    @Email(message = "Incorrect email format")
    @Schema(description = "User email", example = "user@example.com")
    private String email;
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@#$%^&+=!]*$",
            message = "The password must contain letters and numbers"
    )
    @Schema(description = "Password (minimum 8 characters, letters and numbers)", example = "password123")
    private String password;
    @NotBlank(message = "Phone is required")
    @Pattern(
            regexp = "^\\+?\\d{10,15}$",
            message = "Incorrect phone format"
    )
    @Schema(description = "Phone number (10-15 digits, may include +)", example = "+79991234567")
    private String phoneNumber;
}
