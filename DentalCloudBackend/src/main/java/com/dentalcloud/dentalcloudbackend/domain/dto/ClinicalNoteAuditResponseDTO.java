package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ClinicalNoteAuditResponseDTO {
    private UUID id;
    private UUID noteId;
    private String action;
    private UUID actorId;
    private String detail;
    private Instant occurredAt;
}
