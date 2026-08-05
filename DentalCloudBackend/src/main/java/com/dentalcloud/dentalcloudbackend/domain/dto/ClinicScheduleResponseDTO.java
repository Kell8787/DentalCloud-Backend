package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class ClinicScheduleResponseDTO {
    private UUID id;
    private Integer dayOfWeek;
    private LocalTime opensAt;
    private LocalTime closesAt;
    private boolean enabled;
    private Long version;
}
