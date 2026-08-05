package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TreatmentPlanProgressDTO {
    private int completedSteps;
    private int totalSteps;
    private BigDecimal percentage;
}
