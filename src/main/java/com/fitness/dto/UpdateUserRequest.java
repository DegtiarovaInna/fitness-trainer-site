package com.fitness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Name is required")
    @Schema(description = "Username", example = "Alex Frolow")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Incorrect email format")
    @Schema(description = "User email", example = "user@example.com")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(
            regexp = "^\\+?\\d{10,15}$",
            message = "Incorrect phone format"
    )
    @Schema(description = "Phone number (10-15 digits, may include +)", example = "+79991234567")
    private String phoneNumber;

}
