package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ClinicalDocumentUploadRequestDTO {
    private UUID patientId;
    private UUID appointmentId;
    private UUID planId;

    @NotBlank
    @Size(max = 32)
    private String documentType;

    @NotBlank
    @Size(max = 180)
    private String title;

    private boolean visibleToPatient;
}
