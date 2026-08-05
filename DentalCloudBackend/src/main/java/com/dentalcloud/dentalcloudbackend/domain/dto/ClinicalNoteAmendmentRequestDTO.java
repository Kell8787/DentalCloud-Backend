package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClinicalNoteAmendmentRequestDTO {
    @NotBlank
    @Size(max = 20000)
    private String body;

    @NotBlank
    @Size(max = 500)
    private String reason;
}
