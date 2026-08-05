package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import com.dentalcloud.dentalcloudbackend.repositories.CitasRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecretaryDashboardServiceTest {
    @Mock private CitasRepository citasRepository;
    @Mock private CitaService citaService;
    @Mock private InventoryService inventoryService;
    @InjectMocks private SecretaryDashboardService service;

    @Test
    void normalizesRequestedWeekToMondayAndBuildsKpis() {
        LocalDate requested = LocalDate.of(2026, 8, 5);
        when(citasRepository.countByStartsAtGreaterThanEqualAndStartsAtLessThan(any(), any())).thenReturn(8L);
        when(citasRepository.countByStatusAndStartsAtGreaterThanEqualAndStartsAtLessThan(
                any(), any(), any())).thenReturn(2L);
        when(citaService.obtenerTodasLasCitas(any(), eq(null))).thenReturn(List.of());
        when(inventoryService.getAll()).thenReturn(List.of());

        var response = service.get(requested);

        assertThat(response.getWeekOf()).isEqualTo(LocalDate.of(2026, 8, 3));
        assertThat(response.getTotalAppointments()).isEqualTo(8L);
        assertThat(response.getRequestedAppointments()).isEqualTo(2L);
        assertThat(response.getLowStockProducts()).isEmpty();
    }
}
