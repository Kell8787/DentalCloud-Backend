package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class UpdateClinicScheduleRequestDTO {
    @NotNull
    private LocalTime opensAt;

    @NotNull
    private LocalTime closesAt;

    @NotNull
    private Boolean enabled;

    private Long version;
}
