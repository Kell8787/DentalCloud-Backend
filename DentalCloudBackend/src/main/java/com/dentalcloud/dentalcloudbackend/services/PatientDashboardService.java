package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.CitaResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.PatientDashboardResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientDashboardService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/El_Salvador");
    private static final EnumSet<AppointmentStatus> UPCOMING_STATUSES =
            EnumSet.of(AppointmentStatus.SOLICITADA, AppointmentStatus.CONFIRMADA);

    private final TreatmentPlanService treatmentPlanService;
    private final CitaService citaService;
    private final AftercareInstructionService aftercareInstructionService;
    private final ClinicalDocumentService clinicalDocumentService;

    public PatientDashboardResponseDTO get(String email) {
        List<CitaResponseDTO> appointments = citaService.obtenerMisCitas(email);
        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);
        CitaResponseDTO next = appointments.stream()
                .filter(appointment -> UPCOMING_STATUSES.contains(appointment.getStatus()))
                .filter(appointment -> appointment.getStartsAt() != null && !appointment.getStartsAt().isBefore(now))
                .min(java.util.Comparator.comparing(CitaResponseDTO::getStartsAt))
                .orElse(null);
        List<CitaResponseDTO> history = appointments.stream()
                .filter(appointment -> appointment != next)
                .filter(appointment -> appointment.getStartsAt() == null
                        || appointment.getStartsAt().isBefore(now)
                        || !UPCOMING_STATUSES.contains(appointment.getStatus()))
                .toList();
        return PatientDashboardResponseDTO.builder()
                .plans(treatmentPlanService.listOwn(email))
                .nextAppointment(next)
                .appointmentHistory(history)
                .aftercareInstructions(aftercareInstructionService.listOwn(email))
                .documents(clinicalDocumentService.listOwn(email))
                .build();
    }
}
