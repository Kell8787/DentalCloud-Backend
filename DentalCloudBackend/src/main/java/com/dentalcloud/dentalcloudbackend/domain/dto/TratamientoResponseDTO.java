package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TratamientoResponseDTO {
    private UUID id;
    private String nombre;
    private String descripcion;
    private Integer duracionMinutos;
    private BigDecimal precio;
}

