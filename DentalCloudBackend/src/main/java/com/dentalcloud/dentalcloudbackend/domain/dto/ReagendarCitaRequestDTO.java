package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReagendarCitaRequestDTO {
    @NotNull
    @Future
    private LocalDateTime startsAt;

    @NotBlank
    @Size(max = 2000)
    private String reason;
}
