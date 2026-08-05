package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ClinicalDocumentResponseDTO {
    private UUID id;
    private UUID patientId;
    private UUID appointmentId;
    private UUID planId;
    private String documentType;
    private String title;
    private String objectKey;
    private String mimeType;
    private Long sizeBytes;
    private String checksum;
    private boolean visibleToPatient;
    private UUID createdBy;
    private Instant createdAt;
}
