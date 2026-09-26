package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AvailabilitySlotDTO {
    private UUID doctorId;
    private String doctorName;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
}
