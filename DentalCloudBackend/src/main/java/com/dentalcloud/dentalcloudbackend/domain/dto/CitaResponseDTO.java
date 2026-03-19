package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CitaResponseDTO {
    @NotNull
    private UUID pacienteId;

    @NotNull
    private UUID dentistaId;

    @NotNull
    @FutureOrPresent
    private LocalDate fecha;

    @NotNull
    private LocalTime horaDesde;

    @NotNull
    private LocalTime horaHasta;

}
