package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentTransitionRequestDTO {
    @NotNull(message = "El estado destino es requerido")
    private AppointmentStatus targetStatus;

    @Size(max = 2000, message = "El motivo no puede superar 2000 caracteres")
    private String reason;

    private LocalDateTime startsAt;
}
