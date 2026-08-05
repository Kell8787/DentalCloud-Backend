package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class AftercareInstructionRequestDTO {

    @NotNull
    private UUID appointmentId;

    @NotNull
    private UUID patientId;

    @NotBlank
    @Size(max = 180)
    private String title;

    @NotBlank
    private String body;

    @Pattern(regexp = "LOW|NORMAL|HIGH")
    private String priority;

    private boolean publish;
}
