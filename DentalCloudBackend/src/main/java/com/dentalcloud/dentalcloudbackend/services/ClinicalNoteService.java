package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalNoteAmendmentRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalNoteAuditResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalNoteRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalNoteResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.Citas;
import com.dentalcloud.dentalcloudbackend.domain.entity.ClinicalNote;
import com.dentalcloud.dentalcloudbackend.domain.entity.ClinicalNoteAudit;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import com.dentalcloud.dentalcloudbackend.domain.enums.ClinicalNoteStatus;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.exceptions.BusinessException;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.repositories.CitasRepository;
import com.dentalcloud.dentalcloudbackend.repositories.ClinicalNoteAuditRepository;
import com.dentalcloud.dentalcloudbackend.repositories.ClinicalNoteRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClinicalNoteService {
    private final ClinicalNoteRepository noteRepository;
    private final ClinicalNoteAuditRepository auditRepository;
    private final CitasRepository citasRepository;
    private final UserRepository userRepository;

    @Transactional
    public ClinicalNoteResponseDTO create(ClinicalNoteRequestDTO request, String actorEmail) {
        User actor = clinicalActor(actorEmail);
        Citas appointment = appointment(request.getAppointmentId());
        assertCanManage(appointment, actor);
        if (appointment.getStatus() != AppointmentStatus.COMPLETADA) {
            throw new BusinessException("La nota solo puede crearse después de completar la cita");
        }
        Instant now = Instant.now();
        ClinicalNote note = noteRepository.save(ClinicalNote.builder()
                .patientId(appointment.getUser().getId()).appointmentId(appointment.getId())
                .authorId(actor.getId()).status(ClinicalNoteStatus.DRAFT)
                .body(request.getBody().trim()).createdAt(now).updatedAt(now).version(0L).build());
        audit(note, "CREATED", actor, null);
        return map(note);
    }

    @Transactional
    public ClinicalNoteResponseDTO updateDraft(UUID noteId, ClinicalNoteRequestDTO request, String actorEmail) {
        User actor = clinicalActor(actorEmail);
        ClinicalNote note = note(noteId);
        assertCanManage(note, actor);
        if (note.getStatus() != ClinicalNoteStatus.DRAFT) {
            throw new BusinessException("Solo se puede editar una nota en borrador");
        }
        note.setBody(request.getBody().trim());
        note.setUpdatedAt(Instant.now());
        ClinicalNote saved = noteRepository.save(note);
        audit(saved, "UPDATED", actor, null);
        return map(saved);
    }

    @Transactional
    public ClinicalNoteResponseDTO finalize(UUID noteId, String actorEmail) {
        User actor = clinicalActor(actorEmail);
        ClinicalNote note = note(noteId);
        assertCanManage(note, actor);
        if (note.getStatus() != ClinicalNoteStatus.DRAFT) {
            throw new BusinessException("Solo se puede finalizar una nota en borrador");
        }
        Instant now = Instant.now();
        note.setStatus(ClinicalNoteStatus.FINAL);
        note.setFinalizedAt(now);
        note.setUpdatedAt(now);
        ClinicalNote saved = noteRepository.save(note);
        audit(saved, "FINALIZED", actor, null);
        return map(saved);
    }

    @Transactional
    public ClinicalNoteResponseDTO amend(UUID noteId,
                                         ClinicalNoteAmendmentRequestDTO request,
                                         String actorEmail) {
        User actor = clinicalActor(actorEmail);
        ClinicalNote source = note(noteId);
        assertCanManage(source, actor);
        if (source.getStatus() != ClinicalNoteStatus.FINAL
                && source.getStatus() != ClinicalNoteStatus.AMENDED) {
            throw new BusinessException("Solo se puede enmendar una nota final");
        }
        Instant now = Instant.now();
        ClinicalNote amendment = noteRepository.save(ClinicalNote.builder()
                .patientId(source.getPatientId()).appointmentId(source.getAppointmentId())
                .authorId(actor.getId()).status(ClinicalNoteStatus.AMENDED)
                .body(request.getBody().trim()).createdAt(now).updatedAt(now)
                .finalizedAt(now).amendedFromId(source.getId())
                .amendmentReason(request.getReason().trim()).version(0L).build());
        audit(source, "AMENDED", actor, request.getReason().trim());
        audit(amendment, "CREATED_AMENDMENT", actor, request.getReason().trim());
        return map(amendment);
    }

    @Transactional
    public List<ClinicalNoteResponseDTO> listForPatient(UUID patientId, String actorEmail) {
        User actor = clinicalActor(actorEmail);
        return noteRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .filter(note -> canManage(note, actor)).map(this::map).toList();
    }

    @Transactional
    public ClinicalNoteResponseDTO get(UUID noteId, String actorEmail) {
        User actor = clinicalActor(actorEmail);
        ClinicalNote note = note(noteId);
        assertCanManage(note, actor);
        return map(note);
    }

    @Transactional
    public List<ClinicalNoteAuditResponseDTO> audit(UUID noteId, String actorEmail) {
        User actor = clinicalActor(actorEmail);
        ClinicalNote note = note(noteId);
        assertCanManage(note, actor);
        return auditRepository.findByNoteIdOrderByOccurredAtAsc(noteId).stream()
                .map(item -> ClinicalNoteAuditResponseDTO.builder().id(item.getId()).noteId(item.getNoteId())
                        .action(item.getAction()).actorId(item.getActorId()).detail(item.getDetail())
                        .occurredAt(item.getOccurredAt()).build()).toList();
    }

    private User clinicalActor(String email) {
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (actor.getRole() != Rol.DOCTOR && actor.getRole() != Rol.ADMIN) {
            throw new BusinessException("Solo el doctor o administrador puede gestionar notas clínicas");
        }
        return actor;
    }

    private void assertCanManage(ClinicalNote note, User actor) {
        assertCanManage(appointment(note.getAppointmentId()), actor);
    }

    private void assertCanManage(Citas appointment, User actor) {
        if (actor.getRole() == Rol.ADMIN) {
            return;
        }
        if (appointment.getDentist() == null || appointment.getDentist().getUser() == null
                || !appointment.getDentist().getUser().getId().equals(actor.getId())) {
            throw new ResourceNotFoundException("Nota clínica no encontrada");
        }
    }

    private boolean canManage(ClinicalNote note, User actor) {
        try {
            assertCanManage(note, actor);
            return true;
        } catch (ResourceNotFoundException exception) {
            return false;
        }
    }

    private Citas appointment(UUID id) {
        return citasRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
    }

    private ClinicalNote note(UUID id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nota clínica no encontrada"));
    }

    private void audit(ClinicalNote note, String action, User actor, String detail) {
        auditRepository.save(ClinicalNoteAudit.builder().noteId(note.getId()).action(action)
                .actorId(actor.getId()).detail(detail).occurredAt(Instant.now()).build());
    }

    private ClinicalNoteResponseDTO map(ClinicalNote note) {
        return ClinicalNoteResponseDTO.builder().id(note.getId()).patientId(note.getPatientId())
                .appointmentId(note.getAppointmentId()).authorId(note.getAuthorId()).status(note.getStatus())
                .body(note.getBody()).createdAt(note.getCreatedAt()).updatedAt(note.getUpdatedAt())
                .finalizedAt(note.getFinalizedAt()).amendedFromId(note.getAmendedFromId())
                .amendmentReason(note.getAmendmentReason()).build();
    }
}
