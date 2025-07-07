package com.fitness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    @Schema(description="Current Password")
    private String currentPassword;

    @NotBlank
    @Size(min=8) @Pattern(regexp="^(?=.*[A-Za-z])(?=.*\\d).*$")
    @Schema(description="New password (minimum 8 characters, letters + numbers)")
    private String newPassword;

    @NotBlank
    private String newPasswordConfirm;
}
