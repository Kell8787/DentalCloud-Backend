package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.SecretaryDashboardResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import com.dentalcloud.dentalcloudbackend.repositories.CitasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class SecretaryDashboardService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/El_Salvador");

    private final CitasRepository citasRepository;
    private final CitaService citaService;
    private final InventoryService inventoryService;

    public SecretaryDashboardResponseDTO get(LocalDate requestedWeek) {
        LocalDate weekOf = (requestedWeek == null ? LocalDate.now(BUSINESS_ZONE) : requestedWeek)
                .with(DayOfWeek.MONDAY);
        LocalDateTime from = weekOf.atStartOfDay();
        LocalDateTime to = weekOf.plusDays(7).atStartOfDay();
        var inventory = inventoryService.getAll().stream()
                .filter(item -> item.getQuantity() != null && item.getMinimumStock() != null
                        && item.getQuantity() <= item.getMinimumStock())
                .toList();
        return SecretaryDashboardResponseDTO.builder()
                .weekOf(weekOf)
                .totalAppointments(citasRepository.countByStartsAtGreaterThanEqualAndStartsAtLessThan(from, to))
                .requestedAppointments(count(AppointmentStatus.SOLICITADA, from, to))
                .confirmedAppointments(count(AppointmentStatus.CONFIRMADA, from, to))
                .cancelledAppointments(count(AppointmentStatus.CANCELADA, from, to))
                .todayAppointments(citaService.obtenerCitasPorRango(from, to))
                .lowStockProducts(inventory)
                .build();
    }

    private long count(AppointmentStatus status, LocalDateTime from, LocalDateTime to) {
        return citasRepository.countByStatusAndStartsAtGreaterThanEqualAndStartsAtLessThan(status, from, to);
    }
}
