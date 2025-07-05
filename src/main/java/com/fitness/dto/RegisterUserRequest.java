package com.fitness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserRequest {
    @NotBlank(message = "Имя обязательно")
    @Schema(description = "Имя пользователя", example = "Иван Иванов")
    private String name;
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен быть минимум 8 символов")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@#$%^&+=!]*$",
            message = "Пароль должен содержать буквы и цифры"
    )
    @Schema(description = "Пароль (минимум 8 символов, буквы и цифры)", example = "password123")
    private String password;
    @NotBlank(message = "Телефон обязателен")
    @Pattern(
            regexp = "^\\+?\\d{10,15}$",
            message = "Некорректный формат телефона"
    )
    @Schema(description = "Номер телефона (10–15 цифр, может с +)", example = "+79991234567")
    private String phoneNumber;
}
