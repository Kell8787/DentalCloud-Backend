package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class PatientTreatmentPlanRequestDTO {

    @NotNull
    private UUID patientId;

    @NotNull
    private UUID treatmentId;

    @NotNull
    private UUID dentistId;

    private Instant startedAt;

    private Instant expectedEndAt;
}
