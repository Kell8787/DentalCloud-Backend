package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class SlotDisponibleDTO {
    private UUID dentistaId;
    private String dentistaNombre;
    private List<LocalTime> horasDisponibles;
}
