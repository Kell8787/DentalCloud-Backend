package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.AftercareInstructionRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.AftercareInstructionResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.AftercareInstruction;
import com.dentalcloud.dentalcloudbackend.domain.entity.Citas;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.exceptions.BusinessException;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.repositories.AftercareInstructionRepository;
import com.dentalcloud.dentalcloudbackend.repositories.CitasRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AftercareInstructionService {
    private final AftercareInstructionRepository instructionRepository;
    private final CitasRepository citasRepository;
    private final UserRepository userRepository;

    @Transactional
    public AftercareInstructionResponseDTO create(AftercareInstructionRequestDTO request, String actorEmail) {
        User actor = actor(actorEmail);
        assertClinicalActor(actor);
        Citas appointment = appointment(request.getAppointmentId());
        assertCanManage(appointment, actor);
        if (appointment.getStatus() != AppointmentStatus.COMPLETADA) {
            throw new BusinessException("La instrucción solo puede crearse después de completar la cita");
        }
        Instant publishedAt = request.isPublish() ? Instant.now() : null;
        AftercareInstruction instruction = AftercareInstruction.builder()
                .appointmentId(appointment.getId())
                .patientId(appointment.getUser().getId())
                .authorId(actor.getId())
                .title(request.getTitle().trim())
                .body(request.getBody().trim())
                .priority(request.getPriority() == null ? "NORMAL" : request.getPriority())
                .publishedAt(publishedAt)
                .build();
        return map(instructionRepository.save(instruction));
    }

    @Transactional
    public List<AftercareInstructionResponseDTO> listOwn(String email) {
        User patient = actor(email);
        return instructionRepository.findByPatientIdAndPublishedAtIsNotNullOrderByPublishedAtDesc(patient.getId())
                .stream().map(this::map).toList();
    }

    @Transactional
    public List<AftercareInstructionResponseDTO> listForClinicalRecord(UUID patientId, String email) {
        User actor = actor(email);
        return instructionRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .filter(instruction -> canRead(instruction, actor))
                .map(this::map).toList();
    }

    @Transactional
    public List<AftercareInstructionResponseDTO> listForAppointment(UUID appointmentId, String email) {
        User actor = actor(email);
        Citas appointment = appointment(appointmentId);
        assertCanManage(appointment, actor);
        return instructionRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId).stream()
                .map(this::map).toList();
    }

    @Transactional
    public AftercareInstructionResponseDTO get(UUID id, String email) {
        AftercareInstruction instruction = instruction(id);
        assertCanRead(instruction, actor(email));
        return map(instruction);
    }

    @Transactional
    public AftercareInstructionResponseDTO publish(UUID id, String actorEmail) {
        User actor = actor(actorEmail);
        assertClinicalActor(actor);
        AftercareInstruction instruction = instruction(id);
        Citas appointment = appointment(instruction.getAppointmentId());
        assertCanManage(appointment, actor);
        if (appointment.getStatus() != AppointmentStatus.COMPLETADA) {
            throw new BusinessException("La instrucción solo puede publicarse después de completar la cita");
        }
        if (instruction.getPublishedAt() == null) {
            instruction.setPublishedAt(Instant.now());
        }
        return map(instructionRepository.save(instruction));
    }

    private void assertClinicalActor(User actor) {
        if (actor.getRole() != Rol.DOCTOR && actor.getRole() != Rol.ADMIN) {
            throw new BusinessException("Solo el doctor o administrador puede gestionar instrucciones clínicas");
        }
    }

    private void assertCanManage(Citas appointment, User actor) {
        if (actor.getRole() == Rol.ADMIN) {
            return;
        }
        if (appointment.getDentist() == null || appointment.getDentist().getUser() == null
                || !appointment.getDentist().getUser().getId().equals(actor.getId())) {
            throw new ResourceNotFoundException("Cita no encontrada");
        }
    }

    private void assertCanRead(AftercareInstruction instruction, User actor) {
        if (actor.getRole() == Rol.CUSTOMER && instruction.getPatientId().equals(actor.getId())
                && instruction.getPublishedAt() != null) {
            return;
        }
        if (actor.getRole() == Rol.ADMIN) {
            return;
        }
        if (actor.getRole() == Rol.DOCTOR) {
            Citas appointment = appointment(instruction.getAppointmentId());
            assertCanManage(appointment, actor);
            return;
        }
        throw new ResourceNotFoundException("Instrucción no encontrada");
    }

    private boolean canRead(AftercareInstruction instruction, User actor) {
        try {
            assertCanRead(instruction, actor);
            return true;
        } catch (ResourceNotFoundException exception) {
            return false;
        }
    }

    private User actor(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Citas appointment(UUID id) {
        return citasRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
    }

    private AftercareInstruction instruction(UUID id) {
        return instructionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instrucción no encontrada"));
    }

    private AftercareInstructionResponseDTO map(AftercareInstruction instruction) {
        return AftercareInstructionResponseDTO.builder()
                .id(instruction.getId()).appointmentId(instruction.getAppointmentId())
                .patientId(instruction.getPatientId()).authorId(instruction.getAuthorId())
                .title(instruction.getTitle()).body(instruction.getBody()).priority(instruction.getPriority())
                .publishedAt(instruction.getPublishedAt()).createdAt(instruction.getCreatedAt())
                .build();
    }
}
