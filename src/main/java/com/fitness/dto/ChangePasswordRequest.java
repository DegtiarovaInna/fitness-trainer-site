package com.fitness.dto;

import com.fitness.validation.StrongPassword;
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
    @Size(min=8)
    @StrongPassword
    @Schema(
            description = "Password ≥8 characters, score ≥ 3 zxcvbn",
            example = "S3cur3!Pass"
    )
    private String newPassword;

    @NotBlank
    private String newPasswordConfirm;
}
