package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CrearCitasRequestDTO {

    @NotNull(message = "El paciente es requerido")
    private UUID pacienteId;

    @NotNull(message = "El dentista es requerido")
    private UUID dentistaId;

    @NotNull(message = "El tratamiento es requerido")
    private UUID tratamientoId;

    @NotNull(message = "La fecha es requerida")
    private LocalDate fecha;

    @NotNull(message = "La hora de inicio es requerida")
    private LocalTime horaInicio;

    @NotNull(message = "El motivo es requerido")
    private String motivo;
}
