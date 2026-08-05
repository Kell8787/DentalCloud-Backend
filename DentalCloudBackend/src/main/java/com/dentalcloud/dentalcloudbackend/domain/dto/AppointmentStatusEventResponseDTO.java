package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AppointmentStatusEventResponseDTO {
    private UUID id;
    private UUID appointmentId;
    private AppointmentStatus fromStatus;
    private AppointmentStatus toStatus;
    private String reason;
    private UUID actorId;
    private LocalDateTime occurredAt;
}
