package com.fitness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    @Schema(description="Текущий пароль")
    private String currentPassword;

    @NotBlank
    @Size(min=8) @Pattern(regexp="^(?=.*[A-Za-z])(?=.*\\d).*$")
    @Schema(description="Новый пароль (минимум 8 символов, буквы+цифры)")
    private String newPassword;

    @NotBlank
    private String newPasswordConfirm;
}
