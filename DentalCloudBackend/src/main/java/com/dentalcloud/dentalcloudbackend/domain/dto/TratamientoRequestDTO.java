package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Data;

@Data
public class TratamientoRequestDTO {
    private String nombre;
    private String descripcion;
    private Integer duracionMinutos;
    private Double precio;
}
