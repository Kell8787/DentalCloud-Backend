package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentRequestDTO {
    @NotNull(message = "El tratamiento es requerido")
    private UUID treatmentId;

    @NotNull(message = "El doctor es requerido")
    private UUID doctorId;

    /**
     * Optional link used when the patient is continuing an existing plan.
     * A first appointment must not require a clinical plan to exist first.
     */
    private UUID treatmentPlanId;

    @NotNull(message = "El inicio de la cita es requerido")
    @Future(message = "La cita debe estar en el futuro")
    private LocalDateTime startsAt;

    @NotBlank(message = "El motivo es requerido")
    @Size(max = 2000, message = "El motivo no puede superar 2000 caracteres")
    private String reason;
}
