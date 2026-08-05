package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AftercareInstructionResponseDTO {
    private UUID id;
    private UUID appointmentId;
    private UUID patientId;
    private UUID authorId;
    private String title;
    private String body;
    private String priority;
    private Instant publishedAt;
    private Instant createdAt;
}
