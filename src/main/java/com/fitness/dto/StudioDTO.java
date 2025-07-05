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
    @NotBlank(message = "Название студии обязательно")
    @Schema(description = "Название студии", example = "Fitness Pro Studio")
    private String name;
    @NotBlank(message = "Адрес обязателен")
    @Schema(description = "Адрес студии", example = "г. MG, ул. Alls, д.10")
    private String address;
    private Long adminId;



//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }
}
