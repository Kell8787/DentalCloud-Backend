package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.AppointmentRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.AppointmentPageResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.AppointmentStatusEventResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.AvailabilitySlotDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.CancelarCitaRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.CitaResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.CrearCitasRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.EditarCitaRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.RechazarCitasRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ReagendarCitaRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.SlotDisponibleDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.StaffAppointmentRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.AppointmentStatusEvent;
import com.dentalcloud.dentalcloudbackend.domain.entity.Citas;
import com.dentalcloud.dentalcloudbackend.domain.entity.ClinicSchedule;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.PatientTreatmentPlan;
import com.dentalcloud.dentalcloudbackend.domain.entity.Tratamiento;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentSource;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import com.dentalcloud.dentalcloudbackend.domain.enums.EstadoCita;
import com.dentalcloud.dentalcloudbackend.domain.enums.MotivoCancelacion;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentPlanStatus;
import com.dentalcloud.dentalcloudbackend.exceptions.BusinessException;
import com.dentalcloud.dentalcloudbackend.exceptions.ConflictException;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.repositories.CitasRepository;
import com.dentalcloud.dentalcloudbackend.repositories.ClinicScheduleRepository;
import com.dentalcloud.dentalcloudbackend.repositories.AppointmentStatusEventRepository;
import com.dentalcloud.dentalcloudbackend.repositories.DentistRepository;
import com.dentalcloud.dentalcloudbackend.repositories.PatientTreatmentPlanRepository;
import com.dentalcloud.dentalcloudbackend.repositories.TratamientoRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CitaService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/El_Salvador");
    private static final Collection<AppointmentStatus> BLOCKING_STATUSES =
            EnumSet.of(AppointmentStatus.SOLICITADA, AppointmentStatus.CONFIRMADA);

    private final CitasRepository citasRepository;
    private final ClinicScheduleRepository clinicScheduleRepository;
    private final UserRepository userRepository;
    private final TratamientoRepository tratamientoRepository;
    private final DentistRepository dentistRepository;
    private final PatientTreatmentPlanRepository treatmentPlanRepository;
    private final AppointmentStatusEventRepository appointmentStatusEventRepository;

    @Transactional
    public CitaResponseDTO solicitarCita(AppointmentRequestDTO request, String email) {
        return solicitarCita(request, email, null);
    }

    @Transactional
    public CitaResponseDTO solicitarCita(AppointmentRequestDTO request, String email, String idempotencyKey) {
        User patient = userByEmail(email);
        PatientTreatmentPlan plan = treatmentPlanRepository.findById(request.getTreatmentPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan de tratamiento no encontrado"));
        if (!plan.getPatientId().equals(patient.getId())) {
            throw new ResourceNotFoundException("Plan de tratamiento no encontrado");
        }
        if (plan.getStatus() == com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentPlanStatus.PAUSED
                || plan.getStatus() == com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentPlanStatus.COMPLETED
                || plan.getStatus() == com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentPlanStatus.CANCELLED) {
            throw new BusinessException("El plan no admite nuevas citas");
        }

        StaffAppointmentRequestDTO staffRequest = new StaffAppointmentRequestDTO();
        staffRequest.setPatientId(patient.getId());
        staffRequest.setDoctorId(plan.getDentistId());
        staffRequest.setTreatmentId(plan.getTreatmentId());
        staffRequest.setTreatmentPlanId(plan.getId());
        staffRequest.setStartsAt(request.getStartsAt());
        staffRequest.setReason(request.getReason());
        return create(staffRequest, AppointmentSource.PATIENT_REQUEST, idempotencyKey, email);
    }

    @Transactional
    public CitaResponseDTO crearCitaStaff(StaffAppointmentRequestDTO request) {
        return crearCitaStaff(request, null);
    }

    @Transactional
    public CitaResponseDTO crearCitaStaff(StaffAppointmentRequestDTO request, String idempotencyKey) {
        return create(request, AppointmentSource.STAFF_CREATED, idempotencyKey, null);
    }

    @Transactional
    public CitaResponseDTO crearCitaStaff(StaffAppointmentRequestDTO request,
                                          String idempotencyKey,
                                          String actorEmail) {
        return create(request, AppointmentSource.STAFF_CREATED, idempotencyKey, actorEmail);
    }

    /** Compatibilidad temporal con el payload histórico. */
    @Transactional
    public CitaResponseDTO crearCita(CrearCitasRequestDTO request) {
        StaffAppointmentRequestDTO modern = new StaffAppointmentRequestDTO();
        modern.setPatientId(request.getPacienteId());
        modern.setDoctorId(request.getDentistaId());
        modern.setTreatmentId(request.getTratamientoId());
        modern.setStartsAt(LocalDateTime.of(request.getFecha(), request.getHoraInicio()));
        modern.setReason(request.getMotivo());
        return create(modern, AppointmentSource.STAFF_CREATED, null, null);
    }

    private CitaResponseDTO create(StaffAppointmentRequestDTO request,
                                   AppointmentSource source,
                                   String rawIdempotencyKey,
                                   String actorEmail) {
        String idempotencyKey = normalizeIdempotencyKey(rawIdempotencyKey);
        User patient = userRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
        if (idempotencyKey != null) {
            var existing = citasRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                if (!existing.get().getUser().getId().equals(patient.getId())) {
                    throw new ConflictException("IDEMPOTENCY_KEY_REUSED",
                            "La clave de idempotencia ya pertenece a otro paciente.");
                }
                return map(existing.get());
            }
        }

        // Serializa las reservas del mismo doctor antes de comprobar solapamientos.
        Dentist dentist = dentistRepository.findByIdForUpdate(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentista no encontrado"));
        Tratamiento treatment = tratamientoRepository.findByIdAndActiveTrue(request.getTreatmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tratamiento no encontrado"));
        validateTreatmentPlan(request, patient, dentist, treatment);

        LocalDateTime endsAt = validateInterval(request.getStartsAt(), treatment.getDuracionMinutos());
        if (citasRepository.existsByDentistAndStartsAtLessThanAndEndsAtGreaterThanAndStatusIn(
                dentist, endsAt, request.getStartsAt(), BLOCKING_STATUSES)) {
            throw new ConflictException("Ese horario ya no está disponible.");
        }

        Citas appointment = Citas.builder()
                .user(patient)
                .dentist(dentist)
                .tratamiento(treatment)
                .treatmentPlanId(request.getTreatmentPlanId())
                .startsAt(request.getStartsAt())
                .endsAt(endsAt)
                .status(source == AppointmentSource.PATIENT_REQUEST
                        ? AppointmentStatus.SOLICITADA : AppointmentStatus.CONFIRMADA)
                .source(source)
                .motivo(request.getReason())
                .idempotencyKey(idempotencyKey)
                .version(0L)
                .build();
        Citas saved = citasRepository.save(appointment);
        recordEvent(saved, null, saved.getStatus(), null, actorEmail);
        return map(saved);
    }

    private void validateTreatmentPlan(StaffAppointmentRequestDTO request,
                                       User patient,
                                       Dentist dentist,
                                       Tratamiento treatment) {
        if (request.getTreatmentPlanId() == null) {
            return;
        }
        PatientTreatmentPlan plan = treatmentPlanRepository.findById(request.getTreatmentPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan de tratamiento no encontrado"));
        if (!plan.getPatientId().equals(patient.getId())
                || !plan.getDentistId().equals(dentist.getId())
                || !plan.getTreatmentId().equals(treatment.getId())) {
            throw new BusinessException("La cita no coincide con el paciente, doctor o tratamiento del plan");
        }
        if (EnumSet.of(TreatmentPlanStatus.PAUSED, TreatmentPlanStatus.COMPLETED,
                TreatmentPlanStatus.CANCELLED).contains(plan.getStatus())) {
            throw new BusinessException("El plan no admite nuevas citas");
        }
    }

    @Transactional
    public List<AvailabilitySlotDTO> obtenerDisponibilidad(LocalDate date, UUID treatmentId) {
        if (date == null || date.isBefore(today())) {
            throw new BusinessException("La fecha no puede ser en el pasado");
        }
        Tratamiento treatment = tratamientoRepository.findByIdAndActiveTrue(treatmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tratamiento no encontrado"));
        ClinicSchedule schedule = scheduleFor(date.getDayOfWeek());
        if (schedule == null || !schedule.isEnabled()) {
            return List.of();
        }
        LocalTime opening = schedule.getOpensAt();
        LocalTime closing = schedule.getClosesAt();

        List<AvailabilitySlotDTO> slots = new ArrayList<>();
        for (Dentist dentist : dentistRepository.findAll()) {
            LocalDateTime dayStart = LocalDateTime.of(date, opening);
            LocalDateTime dayEnd = LocalDateTime.of(date, closing.plusMinutes(15));
            List<Citas> appointments = citasRepository
                    .findByDentistAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                            dentist, dayStart, dayEnd);
            for (LocalDateTime startsAt = dayStart;
                 !startsAt.plusMinutes(treatment.getDuracionMinutos()).isAfter(dayEnd);
                 startsAt = startsAt.plusMinutes(treatment.getDuracionMinutos())) {
                LocalDateTime slotStartsAt = startsAt;
                LocalDateTime slotEndsAt = startsAt.plusMinutes(treatment.getDuracionMinutos());
                boolean occupied = appointments.stream()
                        .filter(appointment -> BLOCKING_STATUSES.contains(appointment.getStatus()))
                        .anyMatch(appointment -> slotStartsAt.isBefore(appointment.getEndsAt())
                                && slotEndsAt.isAfter(appointment.getStartsAt()));
                if (!occupied && !slotStartsAt.isBefore(now())) {
                    slots.add(AvailabilitySlotDTO.builder()
                            .doctorId(dentist.getId())
                            .doctorName(dentist.getName())
                            .startsAt(slotStartsAt)
                            .endsAt(slotEndsAt)
                            .build());
                }
            }
        }
        return slots;
    }

    @Transactional
    public List<AvailabilitySlotDTO> obtenerDisponibilidadPorPlan(
            LocalDate date, UUID planId, UUID treatmentId, String email) {
        if (planId != null) {
            PatientTreatmentPlan plan = treatmentPlanRepository.findById(planId)
                    .orElseThrow(() -> new ResourceNotFoundException("Plan de tratamiento no encontrado"));
            User actor = userByEmail(email);
            if (actor.getRole() == Rol.CUSTOMER && !plan.getPatientId().equals(actor.getId())) {
                throw new ResourceNotFoundException("Plan de tratamiento no encontrado");
            }
            if (treatmentId != null && !treatmentId.equals(plan.getTreatmentId())) {
                throw new BusinessException("El tratamiento no coincide con el plan");
            }
            if (EnumSet.of(TreatmentPlanStatus.PAUSED, TreatmentPlanStatus.COMPLETED,
                    TreatmentPlanStatus.CANCELLED).contains(plan.getStatus())) {
                throw new BusinessException("El plan no admite nuevas citas");
            }
            treatmentId = plan.getTreatmentId();
        }
        if (treatmentId == null) {
            throw new BusinessException("El plan o tratamiento es requerido");
        }
        return obtenerDisponibilidad(date, treatmentId);
    }

    /** Compatibilidad temporal con el formato agrupado histórico. */
    public List<SlotDisponibleDTO> obtenerSlotsDisponibles(LocalDate date, UUID treatmentId) {
        List<AvailabilitySlotDTO> slots = obtenerDisponibilidad(date, treatmentId);
        return slots.stream()
                .collect(java.util.stream.Collectors.groupingBy(AvailabilitySlotDTO::getDoctorId,
                        java.util.LinkedHashMap::new, java.util.stream.Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    List<AvailabilitySlotDTO> doctorSlots = entry.getValue();
                    return SlotDisponibleDTO.builder()
                            .dentistaId(entry.getKey())
                            .dentistaNombre(doctorSlots.get(0).getDoctorName())
                            .horasDisponibles(doctorSlots.stream().map(slot -> slot.getStartsAt().toLocalTime()).toList())
                            .slots(doctorSlots)
                            .build();
                })
                .toList();
    }

    @Transactional
    public CitaResponseDTO aprobarCita(UUID id) {
        return transition(id, AppointmentStatus.CONFIRMADA, null, null);
    }

    @Transactional
    public CitaResponseDTO aprobarCita(UUID id, String actorEmail) {
        return transition(id, AppointmentStatus.CONFIRMADA, null, actorEmail);
    }

    @Transactional
    public CitaResponseDTO rechazarCita(UUID id, RechazarCitasRequestDTO request) {
        return transition(id, AppointmentStatus.RECHAZADA, request == null ? null : request.getMotivo(), null);
    }

    @Transactional
    public CitaResponseDTO rechazarCita(UUID id, RechazarCitasRequestDTO request, String actorEmail) {
        return transition(id, AppointmentStatus.RECHAZADA,
                request == null ? null : request.getMotivo(), actorEmail);
    }

    @Transactional
    public CitaResponseDTO cancelarCita(UUID id, String email, CancelarCitaRequestDTO request) {
        Citas appointment = appointment(id);
        User actor = userByEmail(email);
        boolean owner = appointment.getUser().getId().equals(actor.getId());
        boolean staff = actor.getRole() == Rol.SECRETARIA || actor.getRole() == Rol.ADMIN
                || actor.getRole() == Rol.DOCTOR;
        if (!owner && !staff) {
            throw new BusinessException("No tienes permiso para cancelar esta cita");
        }
        if (owner && appointment.getStartsAt().isBefore(now().plusHours(24))) {
            throw new BusinessException("No se puede cancelar una cita con menos de 24 horas de anticipación");
        }
        if (!owner && (request == null || request.getMotivoCancelacion() == null)) {
            throw new BusinessException("El motivo es obligatorio para cancelar como personal");
        }
        if (actor.getRole() == Rol.DOCTOR && (appointment.getDentist().getUser() == null
                || !appointment.getDentist().getUser().getId().equals(actor.getId()))) {
            throw new BusinessException("No tienes permiso para cancelar citas de otro doctor");
        }
        if (appointment.getStatus() != AppointmentStatus.SOLICITADA
                && appointment.getStatus() != AppointmentStatus.CONFIRMADA) {
            throw new BusinessException("No se puede cancelar una cita en estado " + appointment.getStatus());
        }
        AppointmentStatus previous = appointment.getStatus();
        appointment.setCancellationReason(request == null || request.getMotivoCancelacion() == null
                ? null : request.getMotivoCancelacion().name());
        appointment.setStatus(AppointmentStatus.CANCELADA);
        Citas saved = citasRepository.save(appointment);
        recordEvent(saved, previous, AppointmentStatus.CANCELADA,
                saved.getCancellationReason(), email);
        return map(saved);
    }

    @Transactional
    public CitaResponseDTO completarCita(UUID id) {
        return transition(id, AppointmentStatus.COMPLETADA, null, null);
    }

    @Transactional
    public CitaResponseDTO completarCita(UUID id, String actorEmail) {
        return transition(id, AppointmentStatus.COMPLETADA, null, actorEmail);
    }

    @Transactional
    public CitaResponseDTO marcarInasistencia(UUID id) {
        return transition(id, AppointmentStatus.INASISTENCIA, null, null);
    }

    @Transactional
    public CitaResponseDTO marcarInasistencia(UUID id, String actorEmail) {
        return transition(id, AppointmentStatus.INASISTENCIA, null, actorEmail);
    }

    private CitaResponseDTO transition(UUID id, AppointmentStatus target, String reason, String actorEmail) {
        Citas appointment = appointment(id);
        if (actorEmail != null) {
            assertDoctorOwnsAppointment(appointment, actorEmail);
        }
        if (!AppointmentStatusMachine.isAllowed(appointment.getStatus(), target)) {
            throw new BusinessException("La transición de " + appointment.getStatus() + " a " + target + " no está permitida");
        }
        if ((target == AppointmentStatus.RECHAZADA || target == AppointmentStatus.CANCELADA)
                && (reason == null || reason.isBlank())) {
            throw new BusinessException("El motivo es obligatorio para esta transición");
        }
        if (target == AppointmentStatus.INASISTENCIA && now().isBefore(appointment.getStartsAt())) {
            throw new BusinessException("La inasistencia solo puede registrarse cuando inicia la cita");
        }
        if (target == AppointmentStatus.COMPLETADA && now().isBefore(appointment.getEndsAt())) {
            throw new BusinessException("La cita solo puede completarse después de su hora de finalización");
        }
        AppointmentStatus previous = appointment.getStatus();
        appointment.setStatus(target);
        if (reason != null && !reason.isBlank()) {
            appointment.setCancellationReason(reason);
        }
        Citas saved = citasRepository.save(appointment);
        recordEvent(saved, previous, target, reason, actorEmail);
        return map(saved);
    }

    @Transactional
    public CitaResponseDTO editarCita(UUID id, String email, EditarCitaRequestDTO request) {
        Citas appointment = appointment(id);
        User actor = userByEmail(email);
        if (!appointment.getUser().getId().equals(actor.getId())
                && actor.getRole() == Rol.CUSTOMER) {
            throw new BusinessException("No tienes permiso para editar esta cita");
        }
        if (appointment.getStatus() != AppointmentStatus.SOLICITADA) {
            throw new BusinessException("Solo se pueden editar citas solicitadas");
        }
        Dentist dentist = request.getDentistaId() == null ? appointment.getDentist()
                : dentistRepository.findById(request.getDentistaId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentista no encontrado"));
        Tratamiento treatment = request.getTratamientoId() == null ? appointment.getTratamiento()
                : tratamientoRepository.findByIdAndActiveTrue(request.getTratamientoId())
                .orElseThrow(() -> new ResourceNotFoundException("Tratamiento no encontrado"));
        LocalDate date = request.getFecha() == null ? appointment.getStartsAt().toLocalDate() : request.getFecha();
        LocalTime time = request.getHoraInicio() == null ? appointment.getStartsAt().toLocalTime() : request.getHoraInicio();
        LocalDateTime startsAt = LocalDateTime.of(date, time);
        LocalDateTime endsAt = validateInterval(startsAt, treatment.getDuracionMinutos());
        if (citasRepository.existsByDentistAndStartsAtLessThanAndEndsAtGreaterThanAndStatusInAndIdNot(
                dentist, endsAt, startsAt, BLOCKING_STATUSES, id)) {
            throw new ConflictException("Ese horario ya no está disponible.");
        }
        appointment.setDentist(dentist);
        appointment.setTratamiento(treatment);
        appointment.setStartsAt(startsAt);
        appointment.setEndsAt(endsAt);
        if (request.getMotivo() != null) {
            appointment.setMotivo(request.getMotivo());
        }
        return map(citasRepository.save(appointment));
    }

    @Transactional
    public CitaResponseDTO reagendarCita(UUID id, ReagendarCitaRequestDTO request, String actorEmail) {
        Citas previous = appointment(id);
        User actor = userByEmail(actorEmail);
        if (actor.getRole() != Rol.SECRETARIA && actor.getRole() != Rol.ADMIN && actor.getRole() != Rol.DOCTOR) {
            throw new BusinessException("Solo el personal puede reagendar citas");
        }
        if (actor.getRole() == Rol.DOCTOR && (previous.getDentist().getUser() == null
                || !previous.getDentist().getUser().getId().equals(actor.getId()))) {
            throw new ResourceNotFoundException("Cita no encontrada");
        }
        if (previous.getStatus() != AppointmentStatus.SOLICITADA
                && previous.getStatus() != AppointmentStatus.CONFIRMADA) {
            throw new BusinessException("No se puede reagendar una cita en estado " + previous.getStatus());
        }
        Tratamiento treatment = tratamientoRepository.findByIdAndActiveTrue(previous.getTratamiento().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tratamiento no encontrado"));
        Dentist dentist = dentistRepository.findByIdForUpdate(previous.getDentist().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentista no encontrado"));
        LocalDateTime endsAt = validateInterval(request.getStartsAt(), treatment.getDuracionMinutos());
        if (citasRepository.existsByDentistAndStartsAtLessThanAndEndsAtGreaterThanAndStatusIn(
                dentist, endsAt, request.getStartsAt(), BLOCKING_STATUSES)) {
            throw new ConflictException("Ese horario ya no está disponible.");
        }

        AppointmentStatus previousStatus = previous.getStatus();
        previous.setStatus(AppointmentStatus.CANCELADA);
        previous.setCancellationReason("REAGENDADA: " + request.getReason().trim());
        Citas cancelled = citasRepository.save(previous);
        recordEvent(cancelled, previousStatus, AppointmentStatus.CANCELADA,
                cancelled.getCancellationReason(), actorEmail);

        Citas replacement = Citas.builder()
                .user(previous.getUser()).dentist(dentist).tratamiento(treatment)
                .treatmentPlanId(previous.getTreatmentPlanId()).startsAt(request.getStartsAt()).endsAt(endsAt)
                .status(AppointmentStatus.CONFIRMADA).source(AppointmentSource.STAFF_CREATED)
                .motivo(previous.getMotivo()).rescheduledFromId(previous.getId()).version(0L).build();
        Citas saved = citasRepository.save(replacement);
        recordEvent(saved, null, AppointmentStatus.CONFIRMADA, "REAGENDADA", actorEmail);
        return map(saved);
    }

    /** Compatibilidad temporal: una eliminación histórica se conserva como cancelación. */
    @Transactional
    public void eliminarCita(UUID id, String email) {
        Citas appointment = appointment(id);
        User actor = userByEmail(email);
        if (!appointment.getUser().getId().equals(actor.getId())) {
            throw new BusinessException("No tienes permiso para cancelar esta cita");
        }
        if (appointment.getStatus() != AppointmentStatus.SOLICITADA) {
            throw new BusinessException("Solo se pueden retirar citas solicitadas");
        }
        appointment.setStatus(AppointmentStatus.CANCELADA);
        appointment.setCancellationReason("LEGACY_DELETE");
        Citas saved = citasRepository.save(appointment);
        recordEvent(saved, AppointmentStatus.SOLICITADA, AppointmentStatus.CANCELADA,
                saved.getCancellationReason(), email);
    }

    public CitaResponseDTO obtenerPorId(UUID id) {
        return map(appointment(id));
    }

    public CitaResponseDTO obtenerPorId(UUID id, String email) {
        Citas appointment = appointment(id);
        User actor = userByEmail(email);
        boolean owner = appointment.getUser().getId().equals(actor.getId());
        boolean ownAgenda = appointment.getDentist().getUser() != null
                && appointment.getDentist().getUser().getId().equals(actor.getId());
        boolean staff = actor.getRole() == Rol.SECRETARIA || actor.getRole() == Rol.ADMIN;
        if (!owner && !ownAgenda && !staff) {
            throw new ResourceNotFoundException("Cita no encontrada");
        }
        return map(appointment);
    }

    public List<CitaResponseDTO> obtenerMisCitas(String email) {
        User patient = userByEmail(email);
        return citasRepository.findByUserAndStatusInOrderByStartsAtAsc(
                        patient, EnumSet.allOf(AppointmentStatus.class)).stream()
                .map(this::map).toList();
    }

    public List<CitaResponseDTO> obtenerCitasDeExpediente(UUID patientId, String actorEmail) {
        User actor = userByEmail(actorEmail);
        List<Citas> appointments = citasRepository.findByUserIdOrderByStartsAtAsc(patientId);
        if (actor.getRole() == Rol.ADMIN) {
            return appointments.stream().map(this::map).toList();
        }
        if (actor.getRole() != Rol.DOCTOR) {
            throw new BusinessException("Solo el doctor o administrador puede consultar expedientes clínicos");
        }
        return appointments.stream()
                .filter(appointment -> appointment.getDentist() != null
                        && appointment.getDentist().getUser() != null
                        && appointment.getDentist().getUser().getId().equals(actor.getId()))
                .map(this::map).toList();
    }

    public List<CitaResponseDTO> obtenerAgendaDoctor(String emailDoctor, LocalDate date) {
        Dentist dentist = dentistRepository.findByUserEmail(emailDoctor)
                .orElseThrow(() -> new ResourceNotFoundException("Dentista no encontrado"));
        List<Citas> appointments;
        if (date == null) {
            appointments = citasRepository.findByDentistAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                    dentist, now(), now().plusDays(1));
        } else {
            appointments = citasRepository.findByDentistAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                    dentist, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        }
        return appointments.stream().map(this::map).toList();
    }

    public List<CitaResponseDTO> obtenerAgendaDoctorPorRango(String emailDoctor,
                                                              LocalDate from,
                                                              LocalDate to) {
        Dentist dentist = dentistRepository.findByUserEmail(emailDoctor)
                .orElseThrow(() -> new ResourceNotFoundException("Dentista no encontrado"));
        return citasRepository.findByDentistAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                        dentist, from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                .stream().map(this::map).toList();
    }

    public List<CitaResponseDTO> obtenerTodasLasCitas(LocalDate date, EstadoCita legacyStatus) {
        List<Citas> appointments;
        if (date == null) {
            appointments = citasRepository.findAll();
        } else {
            appointments = citasRepository.findByStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                    date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        }
        if (legacyStatus != null) {
            AppointmentStatus mapped = fromLegacy(legacyStatus);
            appointments = appointments.stream().filter(item -> item.getStatus() == mapped).toList();
        }
        return appointments.stream().map(this::map).toList();
    }

    @Transactional
    public AppointmentPageResponseDTO listarPaginado(int page,
                                                      int size,
                                                      AppointmentStatus status,
                                                      LocalDate from,
                                                      LocalDate to,
                                                      UUID patientId) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException("La paginación debe usar page >= 0 y size entre 1 y 100");
        }
        if (from != null && to != null && to.isBefore(from)) {
            throw new BusinessException("El rango de fechas no es válido");
        }
        LocalDateTime fromAt = from == null ? null : from.atStartOfDay();
        LocalDateTime toAt = to == null ? null : to.plusDays(1).atStartOfDay();
        var result = citasRepository.search(status, fromAt, toAt, patientId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "startsAt")));
        return AppointmentPageResponseDTO.builder()
                .items(result.getContent().stream().map(this::map).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalItems(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    @Transactional
    public List<AppointmentStatusEventResponseDTO> obtenerHistorial(UUID appointmentId, String email) {
        obtenerPorId(appointmentId, email);
        return appointmentStatusEventRepository.findByAppointmentIdOrderByOccurredAtAsc(appointmentId)
                .stream().map(event -> AppointmentStatusEventResponseDTO.builder()
                        .id(event.getId()).appointmentId(event.getAppointmentId())
                        .fromStatus(event.getFromStatus()).toStatus(event.getToStatus())
                        .reason(event.getReason()).actorId(event.getActorId()).occurredAt(event.getOccurredAt())
                        .build()).toList();
    }

    private Citas appointment(UUID id) {
        return citasRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
    }

    private User userByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private LocalDateTime validateInterval(LocalDateTime startsAt, int durationMinutes) {
        if (startsAt == null || startsAt.isBefore(now())) {
            throw new BusinessException("La cita debe estar en el futuro");
        }
        ClinicSchedule schedule = scheduleFor(startsAt.getDayOfWeek());
        if (schedule == null || !schedule.isEnabled()) {
            throw new BusinessException("La clínica no atiende ese día");
        }
        LocalTime opening = schedule.getOpensAt();
        LocalTime closing = schedule.getClosesAt();
        LocalDateTime endsAt = startsAt.plusMinutes(durationMinutes);
        if (startsAt.toLocalTime().isBefore(opening)
                || endsAt.toLocalTime().isAfter(closing.plusMinutes(15))) {
            throw new BusinessException("La cita está fuera del horario laboral");
        }
        return endsAt;
    }

    private ClinicSchedule scheduleFor(DayOfWeek day) {
        return clinicScheduleRepository.findByDayOfWeek(day.getValue()).orElse(null);
    }

    private LocalDate today() {
        return LocalDate.now(BUSINESS_ZONE);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(BUSINESS_ZONE);
    }

    private String normalizeIdempotencyKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return null;
        }
        String key = rawKey.trim();
        if (key.length() > 100) {
            throw new BusinessException("La clave de idempotencia no puede superar 100 caracteres");
        }
        return key;
    }

    private void assertDoctorOwnsAppointment(Citas appointment, String actorEmail) {
        User actor = userByEmail(actorEmail);
        if (actor.getRole() == Rol.DOCTOR && (appointment.getDentist().getUser() == null
                || !appointment.getDentist().getUser().getId().equals(actor.getId()))) {
            throw new BusinessException("No tienes permiso para gestionar citas de otro doctor");
        }
    }

    private void recordEvent(Citas appointment,
                             AppointmentStatus from,
                             AppointmentStatus to,
                             String reason,
                             String actorEmail) {
        UUID actorId = actorEmail == null ? null : userByEmail(actorEmail).getId();
        appointmentStatusEventRepository.save(AppointmentStatusEvent.builder()
                .appointmentId(appointment.getId())
                .fromStatus(from)
                .toStatus(to)
                .reason(reason)
                .actorId(actorId)
                .occurredAt(LocalDateTime.now(BUSINESS_ZONE))
                .build());
    }

    private CitaResponseDTO map(Citas appointment) {
        return CitaResponseDTO.builder()
                .id(appointment.getId())
                .pacienteId(appointment.getUser().getId())
                .pacienteNombre(appointment.getUser().getFirstName() + " " + appointment.getUser().getLastName())
                .dentistaId(appointment.getDentist().getId())
                .dentistaNombre(appointment.getDentist().getName())
                .tratamientoId(appointment.getTratamiento().getId())
                .tratamientoNombre(appointment.getTratamiento().getNombre())
                .treatmentPlanId(appointment.getTreatmentPlanId())
                .startsAt(appointment.getStartsAt())
                .endsAt(appointment.getEndsAt())
                .status(appointment.getStatus())
                .source(appointment.getSource())
                .rescheduledFromId(appointment.getRescheduledFromId())
                .cancellationReason(appointment.getCancellationReason())
                .fecha(appointment.getStartsAt().toLocalDate())
                .horaInicio(appointment.getStartsAt().toLocalTime())
                .horaFin(appointment.getEndsAt().toLocalTime())
                .duracionMinutos(appointment.getTratamiento().getDuracionMinutos())
                .precio(appointment.getTratamiento().getPrecio())
                .estadoCita(toLegacy(appointment.getStatus()))
                .motivo(appointment.getMotivo())
                .motivoCancelacion(toLegacyReason(appointment.getCancellationReason()))
                .build();
    }

    private EstadoCita toLegacy(AppointmentStatus status) {
        return switch (status) {
            case SOLICITADA -> EstadoCita.PENDIENTE;
            case CONFIRMADA -> EstadoCita.CONFIRMADA;
            case COMPLETADA -> EstadoCita.FINALIZADA;
            case RECHAZADA, CANCELADA, INASISTENCIA -> EstadoCita.CANCELADA;
        };
    }

    private AppointmentStatus fromLegacy(EstadoCita status) {
        return switch (status) {
            case PENDIENTE -> AppointmentStatus.SOLICITADA;
            case CONFIRMADA -> AppointmentStatus.CONFIRMADA;
            case FINALIZADA -> AppointmentStatus.COMPLETADA;
            case CANCELADA, ELIMINADA -> AppointmentStatus.CANCELADA;
        };
    }

    private MotivoCancelacion toLegacyReason(String reason) {
        if (reason == null) return null;
        try {
            return MotivoCancelacion.valueOf(reason);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
