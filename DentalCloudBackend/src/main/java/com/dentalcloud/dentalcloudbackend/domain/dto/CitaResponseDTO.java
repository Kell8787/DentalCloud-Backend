package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentSource;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import com.dentalcloud.dentalcloudbackend.domain.enums.EstadoCita;
import com.dentalcloud.dentalcloudbackend.domain.enums.MotivoCancelacion;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private UUID treatmentPlanId;

    private LocalDateTime startsAt;
    private LocalDateTime endsAt;

    private AppointmentStatus status;
    private AppointmentSource source;
    private UUID rescheduledFromId;
    private String cancellationReason;

    @Deprecated
    private LocalDate fecha;
    @Deprecated
    private LocalTime horaInicio;
    @Deprecated
    private LocalTime horaFin;

    private Integer duracionMinutos;
    private BigDecimal precio;
    private String motivo;
    private EstadoCita estadoCita;

    private MotivoCancelacion motivoCancelacion;

}
