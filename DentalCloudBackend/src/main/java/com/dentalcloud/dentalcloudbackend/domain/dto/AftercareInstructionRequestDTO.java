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

    @NotBlank
    @Size(max = 180)
    private String title;

    @NotBlank
    @Size(max = 10000)
    private String body;

    @Pattern(regexp = "LOW|NORMAL|HIGH")
    private String priority;

    private boolean publish;
}
