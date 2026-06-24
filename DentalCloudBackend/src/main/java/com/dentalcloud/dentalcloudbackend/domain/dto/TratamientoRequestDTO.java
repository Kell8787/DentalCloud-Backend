package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TratamientoRequestDTO {
    @NotBlank
    private String nombre;
    private String descripcion;
    @NotNull
    private Integer duracionMinutos;
    @NotNull
    private BigDecimal precio;
}
