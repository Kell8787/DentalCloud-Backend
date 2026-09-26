package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ClinicalDocumentRequestDTO {

    @NotNull
    private UUID patientId;

    private UUID appointmentId;

    private UUID planId;

    @NotBlank
    @Size(max = 32)
    private String documentType;

    @NotBlank
    @Size(max = 180)
    private String title;

    @NotBlank
    @Size(max = 512)
    private String objectKey;

    @NotBlank
    @Size(max = 128)
    private String mimeType;

    @NotNull
    @Positive
    private Long sizeBytes;

    @NotBlank
    @Size(max = 128)
    private String checksum;

    private boolean visibleToPatient;

    @AssertTrue(message = "El documento debe relacionarse con una cita o un plan")
    public boolean hasParentReference() {
        return appointmentId != null || planId != null;
    }
}
