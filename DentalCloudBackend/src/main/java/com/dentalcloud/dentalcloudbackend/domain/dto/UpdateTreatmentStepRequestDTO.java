package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentStepStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTreatmentStepRequestDTO {
    private TreatmentStepStatus status;

    @Size(max = 2000)
    private String observation;
}
