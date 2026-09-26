package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentPlanStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateTreatmentPlanRequestDTO {
    private TreatmentPlanStatus status;
    private Instant startedAt;
    private Instant expectedEndAt;

    @Size(max = 2000)
    private String cancellationReason;
}
