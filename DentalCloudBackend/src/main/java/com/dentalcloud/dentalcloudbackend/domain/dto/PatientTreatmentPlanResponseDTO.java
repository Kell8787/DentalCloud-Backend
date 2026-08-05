package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentPlanStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PatientTreatmentPlanResponseDTO {
    private UUID id;
    private UUID patientId;
    private UUID treatmentId;
    private UUID dentistId;
    private TreatmentPlanStatus status;
    private Instant startedAt;
    private Instant expectedEndAt;
    private String cancellationReason;
    private Instant createdAt;
    private List<TreatmentStepResponseDTO> steps;
}
