package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudioCreateUpdateDTO {
    @NotBlank(message = "Название студии обязательно")
    private String name;

    @NotBlank(message = "Адрес обязателен")
    private String address;
}
