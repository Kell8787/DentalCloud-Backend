package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CrearCitasRequestDTO {

    private UUID pacienteId;
    private UUID dentistaId;
    private UUID tratamientoId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private String motivo;
}
