package com.fitness.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudioDTO {
    private Long id;
    @NotBlank(message = "Studio name is required")
    @Schema(description = "Studio name", example = "Fitness Pro Studio")
    private String name;
    @NotBlank(message = "Address is required")
    @Schema(description = "Studio address", example = "New York. Alls,10")
    private String address;
    private Long adminId;

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }
}
