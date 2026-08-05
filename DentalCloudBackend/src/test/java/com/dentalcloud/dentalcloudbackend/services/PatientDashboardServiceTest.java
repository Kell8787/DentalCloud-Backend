package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.CitaResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientDashboardServiceTest {
    @Mock private TreatmentPlanService treatmentPlanService;
    @Mock private CitaService citaService;
    @Mock private AftercareInstructionService aftercareInstructionService;
    @Mock private ClinicalDocumentService clinicalDocumentService;
    @InjectMocks private PatientDashboardService service;

    @Test
    void selectsClosestFutureAppointmentAndSeparatesHistory() {
        String email = "patient@example.com";
        CitaResponseDTO next = CitaResponseDTO.builder()
                .id(java.util.UUID.randomUUID()).status(AppointmentStatus.CONFIRMADA)
                .startsAt(LocalDateTime.now().plusHours(2)).build();
        CitaResponseDTO old = CitaResponseDTO.builder()
                .id(java.util.UUID.randomUUID()).status(AppointmentStatus.COMPLETADA)
                .startsAt(LocalDateTime.now().minusDays(1)).build();
        when(citaService.obtenerMisCitas(email)).thenReturn(List.of(old, next));
        when(treatmentPlanService.listOwn(email)).thenReturn(List.of());
        when(aftercareInstructionService.listOwn(email)).thenReturn(List.of());
        when(clinicalDocumentService.listOwn(email)).thenReturn(List.of());

        var response = service.get(email);

        assertThat(response.getNextAppointment()).isEqualTo(next);
        assertThat(response.getAppointmentHistory()).containsExactly(old);
    }
}
