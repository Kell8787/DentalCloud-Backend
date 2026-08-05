package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.CreateTreatmentPlanRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.CreateTreatmentStepRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.PatientTreatmentPlanResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.TreatmentPlanProgressDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.TreatmentStepResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UpdateTreatmentPlanRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UpdateTreatmentStepRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.PatientTreatmentPlan;
import com.dentalcloud.dentalcloudbackend.domain.entity.TreatmentStep;
import com.dentalcloud.dentalcloudbackend.domain.entity.Tratamiento;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentPlanStatus;
import com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentStepStatus;
import com.dentalcloud.dentalcloudbackend.exceptions.BusinessException;
import com.dentalcloud.dentalcloudbackend.exceptions.ConflictException;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.repositories.DentistRepository;
import com.dentalcloud.dentalcloudbackend.repositories.PatientTreatmentPlanRepository;
import com.dentalcloud.dentalcloudbackend.repositories.TreatmentStepRepository;
import com.dentalcloud.dentalcloudbackend.repositories.TratamientoRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TreatmentPlanService {
    private final PatientTreatmentPlanRepository planRepository;
    private final TreatmentStepRepository stepRepository;
    private final UserRepository userRepository;
    private final DentistRepository dentistRepository;
    private final TratamientoRepository tratamientoRepository;

    @Transactional
    public PatientTreatmentPlanResponseDTO create(UUID patientId,
                                                   CreateTreatmentPlanRequestDTO request,
                                                   String actorEmail) {
        User actor = actor(actorEmail);
        assertClinicalActor(actor);
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
        Dentist dentist = dentistRepository.findById(request.getDentistId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentista no encontrado"));
        assertDoctorOwnsDentist(actor, dentist);
        tratamientoRepository.findByIdAndActiveTrue(request.getTreatmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Tratamiento no encontrado"));
        validateDates(request.getStartedAt(), request.getExpectedEndAt());

        PatientTreatmentPlan plan = PatientTreatmentPlan.builder()
                .patientId(patient.getId())
                .treatmentId(request.getTreatmentId())
                .dentistId(dentist.getId())
                .startedAt(request.getStartedAt())
                .expectedEndAt(request.getExpectedEndAt())
                .version(0L)
                .build();
        return map(planRepository.save(plan));
    }

    @Transactional
    public List<PatientTreatmentPlanResponseDTO> listOwn(String email) {
        User patient = actor(email);
        return planRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId())
                .stream().map(this::map).toList();
    }

    @Transactional
    public List<PatientTreatmentPlanResponseDTO> listForPatient(UUID patientId, String email) {
        User actor = actor(email);
        assertClinicalActor(actor);
        List<PatientTreatmentPlan> plans = planRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        if (actor.getRole() == Rol.DOCTOR) {
            UUID dentistId = dentistRepository.findByUser(actor)
                    .orElseThrow(() -> new ResourceNotFoundException("Dentista no encontrado")).getId();
            plans = plans.stream().filter(plan -> plan.getDentistId().equals(dentistId)).toList();
        }
        return plans.stream().map(this::map).toList();
    }

    @Transactional
    public PatientTreatmentPlanResponseDTO get(UUID planId, String email) {
        PatientTreatmentPlan plan = plan(planId);
        assertCanRead(plan, actor(email));
        return map(plan);
    }

    @Transactional
    public PatientTreatmentPlanResponseDTO update(UUID planId,
                                                  UpdateTreatmentPlanRequestDTO request,
                                                  String actorEmail) {
        PatientTreatmentPlan plan = plan(planId);
        User actor = actor(actorEmail);
        assertCanWrite(plan, actor);
        if (plan.getStatus() == TreatmentPlanStatus.COMPLETED
                || plan.getStatus() == TreatmentPlanStatus.CANCELLED) {
            throw new BusinessException("No se pueden modificar pasos de un plan cerrado");
        }
        if (request.getStatus() != null) {
            if (!allowedTransition(plan.getStatus(), request.getStatus())) {
                throw new BusinessException("La transición del plan no está permitida");
            }
            if (request.getStatus() == TreatmentPlanStatus.CANCELLED
                    && (request.getCancellationReason() == null || request.getCancellationReason().isBlank())) {
                throw new BusinessException("El motivo de cancelación es obligatorio");
            }
            if (request.getStatus() == TreatmentPlanStatus.COMPLETED
                    && progress(plan).getCompletedSteps() < progress(plan).getTotalSteps()) {
                throw new BusinessException("No se puede completar un plan con pasos pendientes");
            }
            plan.setStatus(request.getStatus());
        }
        if (request.getStartedAt() != null || request.getExpectedEndAt() != null) {
            Instant startedAt = request.getStartedAt() == null ? plan.getStartedAt() : request.getStartedAt();
            Instant expectedEndAt = request.getExpectedEndAt() == null ? plan.getExpectedEndAt() : request.getExpectedEndAt();
            validateDates(startedAt, expectedEndAt);
            plan.setStartedAt(startedAt);
            plan.setExpectedEndAt(expectedEndAt);
        }
        if (request.getCancellationReason() != null) {
            plan.setCancellationReason(request.getCancellationReason());
        }
        return map(planRepository.save(plan));
    }

    @Transactional
    public TreatmentStepResponseDTO addStep(UUID planId,
                                            CreateTreatmentStepRequestDTO request,
                                            String actorEmail) {
        PatientTreatmentPlan plan = plan(planId);
        assertCanWrite(plan, actor(actorEmail));
        if (stepRepository.existsByPlanIdAndPosition(planId, request.getPosition())) {
            throw new ConflictException("TREATMENT_STEP_POSITION_TAKEN",
                    "Ya existe un paso con esa posición en el plan.");
        }
        TreatmentStep step = TreatmentStep.builder()
                .planId(planId)
                .title(request.getTitle())
                .position(request.getPosition())
                .observation(request.getObservation())
                .version(0L)
                .build();
        return mapStep(stepRepository.save(step));
    }

    @Transactional
    public TreatmentStepResponseDTO updateStep(UUID planId,
                                               UUID stepId,
                                               UpdateTreatmentStepRequestDTO request,
                                               String actorEmail) {
        PatientTreatmentPlan plan = plan(planId);
        User actor = actor(actorEmail);
        assertCanWrite(plan, actor);
        if (plan.getStatus() == TreatmentPlanStatus.COMPLETED
                || plan.getStatus() == TreatmentPlanStatus.CANCELLED) {
            throw new BusinessException("No se pueden modificar pasos de un plan cerrado");
        }
        TreatmentStep step = stepRepository.findById(stepId)
                .filter(item -> item.getPlanId().equals(planId))
                .orElseThrow(() -> new ResourceNotFoundException("Paso no encontrado"));
        TreatmentStepStatus status = request.getStatus() == null ? step.getStatus() : request.getStatus();
        step.setStatus(status);
        if (request.getObservation() != null) {
            step.setObservation(request.getObservation());
        }
        if (status == TreatmentStepStatus.COMPLETED) {
            step.setCompletedAt(Instant.now());
            step.setCompletedBy(actor.getId());
        } else {
            step.setCompletedAt(null);
            step.setCompletedBy(null);
        }
        return mapStep(stepRepository.save(step));
    }

    private PatientTreatmentPlan plan(UUID planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de tratamiento no encontrado"));
    }

    private User actor(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private void assertClinicalActor(User actor) {
        if (actor.getRole() != Rol.DOCTOR && actor.getRole() != Rol.ADMIN) {
            throw new BusinessException("Solo el doctor o administrador puede gestionar planes clínicos");
        }
    }

    private void assertDoctorOwnsDentist(User actor, Dentist dentist) {
        if (actor.getRole() == Rol.DOCTOR) {
            if (dentist.getUser() == null || !dentist.getUser().getId().equals(actor.getId())) {
                throw new BusinessException("No puedes gestionar el plan de otro doctor");
            }
        }
    }

    private void assertCanRead(PatientTreatmentPlan plan, User actor) {
        if (actor.getRole() == Rol.CUSTOMER && !plan.getPatientId().equals(actor.getId())) {
            throw new ResourceNotFoundException("Plan de tratamiento no encontrado");
        }
        if (actor.getRole() == Rol.DOCTOR) {
            UUID dentistId = dentistRepository.findByUser(actor)
                    .orElseThrow(() -> new ResourceNotFoundException("Dentista no encontrado")).getId();
            if (!plan.getDentistId().equals(dentistId)) {
                throw new ResourceNotFoundException("Plan de tratamiento no encontrado");
            }
        }
        if (actor.getRole() != Rol.CUSTOMER && actor.getRole() != Rol.DOCTOR && actor.getRole() != Rol.ADMIN) {
            throw new BusinessException("No tienes permiso para consultar este plan");
        }
    }

    private void assertCanWrite(PatientTreatmentPlan plan, User actor) {
        assertClinicalActor(actor);
        if (actor.getRole() == Rol.ADMIN) {
            return;
        }
        assertDoctorOwnsDentist(actor, dentistRepository.findById(plan.getDentistId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentista no encontrado")));
    }

    private void validateDates(Instant startedAt, Instant expectedEndAt) {
        if (startedAt != null && expectedEndAt != null && expectedEndAt.isBefore(startedAt)) {
            throw new BusinessException("La fecha estimada no puede ser anterior al inicio");
        }
    }

    private boolean allowedTransition(TreatmentPlanStatus from, TreatmentPlanStatus to) {
        return switch (from) {
            case PLANNED -> to == TreatmentPlanStatus.ACTIVE || to == TreatmentPlanStatus.CANCELLED;
            case ACTIVE -> to == TreatmentPlanStatus.PAUSED
                    || to == TreatmentPlanStatus.COMPLETED || to == TreatmentPlanStatus.CANCELLED;
            case PAUSED -> to == TreatmentPlanStatus.ACTIVE || to == TreatmentPlanStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private TreatmentPlanProgressDTO progress(PatientTreatmentPlan plan) {
        return progress(plan.getId());
    }

    private TreatmentPlanProgressDTO progress(UUID planId) {
        List<TreatmentStep> steps = stepRepository.findByPlanIdOrderByPositionAsc(planId);
        int total = steps.size();
        int completed = (int) steps.stream().filter(step -> step.getStatus() == TreatmentStepStatus.COMPLETED).count();
        BigDecimal percentage = total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(completed * 100L)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        return TreatmentPlanProgressDTO.builder()
                .completedSteps(completed)
                .totalSteps(total)
                .percentage(percentage)
                .build();
    }

    private PatientTreatmentPlanResponseDTO map(PatientTreatmentPlan plan) {
        String treatmentName = tratamientoRepository.findById(plan.getTreatmentId())
                .map(Tratamiento::getNombre)
                .orElse("Tratamiento asignado");
        String dentistName = dentistRepository.findById(plan.getDentistId())
                .map(Dentist::getName)
                .orElse("Doctor asignado");
        return PatientTreatmentPlanResponseDTO.builder()
                .id(plan.getId())
                .patientId(plan.getPatientId())
                .treatmentId(plan.getTreatmentId())
                .treatmentName(treatmentName)
                .dentistId(plan.getDentistId())
                .dentistName(dentistName)
                .status(plan.getStatus())
                .startedAt(plan.getStartedAt())
                .expectedEndAt(plan.getExpectedEndAt())
                .cancellationReason(plan.getCancellationReason())
                .createdAt(plan.getCreatedAt())
                .steps(stepRepository.findByPlanIdOrderByPositionAsc(plan.getId()).stream().map(this::mapStep).toList())
                .progress(progress(plan))
                .version(plan.getVersion())
                .build();
    }

    private TreatmentStepResponseDTO mapStep(TreatmentStep step) {
        return TreatmentStepResponseDTO.builder()
                .id(step.getId())
                .planId(step.getPlanId())
                .title(step.getTitle())
                .position(step.getPosition())
                .status(step.getStatus())
                .completedAt(step.getCompletedAt())
                .completedBy(step.getCompletedBy())
                .observation(step.getObservation())
                .createdAt(step.getCreatedAt())
                .progress(progress(step.getPlanId()))
                .version(step.getVersion())
                .build();
    }
}
