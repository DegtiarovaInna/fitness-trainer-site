package com.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudioCreateUpdateDTO {
    @NotBlank(message = "Studio name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;
}
