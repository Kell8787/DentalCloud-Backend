package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.dentalcloud.dentalcloudbackend.domain.enums.ClinicalNoteStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ClinicalNoteResponseDTO {
    private UUID id;
    private UUID patientId;
    private UUID appointmentId;
    private UUID authorId;
    private ClinicalNoteStatus status;
    private String body;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant finalizedAt;
    private UUID amendedFromId;
    private String amendmentReason;
}
