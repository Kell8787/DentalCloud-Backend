package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicScheduleResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UpdateClinicScheduleRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.ClinicSchedule;
import com.dentalcloud.dentalcloudbackend.exceptions.BusinessException;
import com.dentalcloud.dentalcloudbackend.exceptions.ConflictException;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.repositories.ClinicScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClinicScheduleService {
    private final ClinicScheduleRepository repository;

    public List<ClinicScheduleResponseDTO> list() {
        return repository.findAll().stream()
                .sorted(java.util.Comparator.comparing(ClinicSchedule::getDayOfWeek))
                .map(this::map)
                .toList();
    }

    @Transactional
    public ClinicScheduleResponseDTO update(int dayOfWeek, UpdateClinicScheduleRequestDTO request) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new BusinessException("El día de la semana debe estar entre 1 y 7.");
        }
        if (request.getOpensAt() == null || request.getClosesAt() == null
                || !request.getOpensAt().isBefore(request.getClosesAt())) {
            throw new BusinessException("La hora de apertura debe ser anterior al cierre.");
        }

        ClinicSchedule schedule = repository.findByDayOfWeek(dayOfWeek)
                .orElseThrow(() -> new ResourceNotFoundException("No existe configuración para ese día."));
        if (request.getVersion() != null && !request.getVersion().equals(schedule.getVersion())) {
            throw new ConflictException("CLINIC_SCHEDULE_VERSION_CONFLICT",
                    "El horario cambió mientras lo editabas. Recarga la configuración e inténtalo de nuevo.");
        }
        schedule.setOpensAt(request.getOpensAt());
        schedule.setClosesAt(request.getClosesAt());
        schedule.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
        return map(repository.save(schedule));
    }

    private ClinicScheduleResponseDTO map(ClinicSchedule schedule) {
        return ClinicScheduleResponseDTO.builder()
                .id(schedule.getId())
                .dayOfWeek(schedule.getDayOfWeek())
                .opensAt(schedule.getOpensAt())
                .closesAt(schedule.getClosesAt())
                .enabled(schedule.isEnabled())
                .version(schedule.getVersion())
                .build();
    }
}
