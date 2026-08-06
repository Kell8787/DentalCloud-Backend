package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.entity.Citas;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import com.dentalcloud.dentalcloudbackend.exceptions.BusinessException;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.repositories.CitasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Fixture-only support for browser tests. It is not loaded outside the e2e profile.
 */
@Service
@Profile("e2e")
@RequiredArgsConstructor
public class E2eAppointmentFixtureService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/El_Salvador");

    private final CitasRepository citasRepository;

    @Transactional
    public Citas rewindToCompletable(UUID appointmentId) {
        Citas appointment = citasRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        if (appointment.getStatus() != AppointmentStatus.CONFIRMADA) {
            throw new BusinessException("La fixture solo admite citas confirmadas");
        }

        LocalDateTime endsAt = LocalDateTime.now(BUSINESS_ZONE).minusMinutes(1);
        appointment.setEndsAt(endsAt);
        appointment.setStartsAt(endsAt.minusMinutes(appointment.getTratamiento().getDuracionMinutos()));
        return citasRepository.save(appointment);
    }
}
