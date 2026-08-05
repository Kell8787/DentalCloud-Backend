package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SecretaryDashboardResponseDTO {
    private LocalDate weekOf;
    private long totalAppointments;
    private long requestedAppointments;
    private long confirmedAppointments;
    private long cancelledAppointments;
    private List<CitaResponseDTO> todayAppointments;
    private List<InventoryResponseDTO> lowStockProducts;
}
