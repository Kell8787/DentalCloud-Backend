package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.dentalcloud.dentalcloudbackend.domain.enums.EstadoCita;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class    CitaResponseDTO {

    private UUID id;
    
    @NotNull
    private UUID pacienteId;

    @NotNull
    private UUID dentistaId;

    @NotNull
    private UUID tratamientoId;

    @NotNull
    @FutureOrPresent
    private LocalDate fecha;

    @NotNull
    private LocalTime horaInicio;

    private Integer duracionMinutos;

    private BigDecimal precio;

    private String motivo;

    private EstadoCita estadoCita;


}
