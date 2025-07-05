package com.fitness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Имя обязательно")
    @Schema(description = "Имя пользователя", example = "Иван Иванов")
    private String name;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;

    @NotBlank(message = "Телефон обязателен")
    @Pattern(
            regexp = "^\\+?\\d{10,15}$",
            message = "Некорректный формат телефона"
    )
    @Schema(description = "Номер телефона (10–15 цифр, может с +)", example = "+79991234567")
    private String phoneNumber;

}
