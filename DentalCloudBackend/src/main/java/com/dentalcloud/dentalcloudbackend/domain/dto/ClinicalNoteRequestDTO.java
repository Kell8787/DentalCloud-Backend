package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ClinicalNoteRequestDTO {
    @NotNull
    private UUID appointmentId;

    @NotBlank
    @Size(max = 20000)
    private String body;
}
