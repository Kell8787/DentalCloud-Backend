package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.dentalcloud.dentalcloudbackend.domain.enums.EstadoCita;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class CitaResponseDTO {

    private UUID id;

    private UUID pacienteId;
    private String pacienteNombre;

    private UUID dentistaId;
    private String dentistaNombre;

    private UUID tratamientoId;
    private String tratamientoNombre;

    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    private Integer duracionMinutos;
    private BigDecimal precio;
    private String motivo;
    private EstadoCita estadoCita;
}
