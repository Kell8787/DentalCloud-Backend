package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTreatmentStepRequestDTO {
    @NotBlank
    @Size(max = 180)
    private String title;

    @NotNull
    @Min(1)
    private Integer position;

    @Size(max = 2000)
    private String observation;
}
