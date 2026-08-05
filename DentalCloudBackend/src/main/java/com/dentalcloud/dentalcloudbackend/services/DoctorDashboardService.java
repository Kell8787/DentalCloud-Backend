package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.CitaResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.DoctorAgendaDayResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.DoctorDashboardResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import com.dentalcloud.dentalcloudbackend.exceptions.BusinessException;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.repositories.DentistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorDashboardService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/El_Salvador");
    private static final int MAX_RANGE_DAYS = 31;
    private static final EnumSet<AppointmentStatus> UPCOMING_STATUSES =
            EnumSet.of(AppointmentStatus.SOLICITADA, AppointmentStatus.CONFIRMADA);

    private final DentistRepository dentistRepository;
    private final CitaService citaService;

    public DoctorDashboardResponseDTO get(String doctorEmail, LocalDate requestedFrom, LocalDate requestedTo) {
        Dentist dentist = dentistRepository.findByUserEmail(doctorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Dentista no encontrado"));
        DateRange range = resolveRange(requestedFrom, requestedTo);
        List<CitaResponseDTO> appointments = citaService.obtenerAgendaDoctorPorRango(
                doctorEmail, range.from(), range.to());

        Map<LocalDate, List<CitaResponseDTO>> byDay = new LinkedHashMap<>();
        for (LocalDate date = range.from(); !date.isAfter(range.to()); date = date.plusDays(1)) {
            LocalDate currentDate = date;
            byDay.put(currentDate, appointments.stream()
                    .filter(appointment -> appointment.getStartsAt().toLocalDate().equals(currentDate))
                    .toList());
        }

        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);
        List<CitaResponseDTO> nextAppointments = appointments.stream()
                .filter(appointment -> appointment.getStartsAt() != null
                        && !appointment.getStartsAt().isBefore(now)
                        && UPCOMING_STATUSES.contains(appointment.getStatus()))
                .limit(5)
                .toList();

        List<DoctorAgendaDayResponseDTO> calendar = byDay.entrySet().stream()
                .map(entry -> DoctorAgendaDayResponseDTO.builder()
                        .date(entry.getKey()).appointments(entry.getValue()).build())
                .toList();
        return DoctorDashboardResponseDTO.builder()
                .doctorId(dentist.getId())
                .doctorName(dentist.getName())
                .from(range.from())
                .to(range.to())
                .nextAppointments(nextAppointments)
                .calendar(calendar)
                .build();
    }

    private DateRange resolveRange(LocalDate requestedFrom, LocalDate requestedTo) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate from;
        LocalDate to;
        if (requestedFrom == null && requestedTo == null) {
            from = today.with(DayOfWeek.MONDAY);
            to = from.plusDays(6);
        } else if (requestedFrom == null) {
            from = requestedTo;
            to = requestedTo;
        } else if (requestedTo == null) {
            from = requestedFrom;
            to = requestedFrom;
        } else {
            from = requestedFrom;
            to = requestedTo;
        }
        if (to.isBefore(from)) {
            throw new BusinessException("El rango de fechas del dashboard no es válido");
        }
        if (from.plusDays(MAX_RANGE_DAYS - 1L).isBefore(to)) {
            throw new BusinessException("El rango del dashboard no puede superar 31 días");
        }
        return new DateRange(from, to);
    }

    private record DateRange(LocalDate from, LocalDate to) {
    }
}
