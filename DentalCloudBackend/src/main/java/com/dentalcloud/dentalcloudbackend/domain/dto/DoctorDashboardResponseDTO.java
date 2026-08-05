package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DoctorDashboardResponseDTO {
    private UUID doctorId;
    private String doctorName;
    private LocalDate from;
    private LocalDate to;
    private List<CitaResponseDTO> nextAppointments;
    private List<DoctorAgendaDayResponseDTO> calendar;
}
