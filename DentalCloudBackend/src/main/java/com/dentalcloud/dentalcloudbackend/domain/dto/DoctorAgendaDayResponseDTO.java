package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DoctorAgendaDayResponseDTO {
    private LocalDate date;
    private List<CitaResponseDTO> appointments;
}
