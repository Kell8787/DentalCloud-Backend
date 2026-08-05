package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentStepStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TreatmentStepResponseDTO {
    private UUID id;
    private UUID planId;
    private String title;
    private Integer position;
    private TreatmentStepStatus status;
    private Instant completedAt;
    private UUID completedBy;
    private String observation;
    private Instant createdAt;
    private Long version;
}
