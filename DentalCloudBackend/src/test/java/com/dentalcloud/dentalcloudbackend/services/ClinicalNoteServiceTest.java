package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalNoteAmendmentRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalNoteRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.Citas;
import com.dentalcloud.dentalcloudbackend.domain.entity.ClinicalNote;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import com.dentalcloud.dentalcloudbackend.domain.enums.ClinicalNoteStatus;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.repositories.CitasRepository;
import com.dentalcloud.dentalcloudbackend.repositories.ClinicalNoteAuditRepository;
import com.dentalcloud.dentalcloudbackend.repositories.ClinicalNoteRepository;
import com.dentalcloud.dentalcloudbackend.repositories.DentistRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicalNoteServiceTest {
    @Mock private ClinicalNoteRepository noteRepository;
    @Mock private ClinicalNoteAuditRepository auditRepository;
    @Mock private CitasRepository citasRepository;
    @Mock private DentistRepository dentistRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ClinicalNoteService service;

    @Test
    void createsDraftFinalizesAndAmendsWithoutOverwritingFinalNote() {
        UUID appointmentId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();
        User doctor = User.builder().id(UUID.randomUUID()).email("doctor@example.com").role(Rol.DOCTOR).build();
        Citas appointment = appointment(appointmentId, doctor, AppointmentStatus.COMPLETADA);
        ClinicalNote[] stored = new ClinicalNote[1];
        when(userRepository.findByEmail(doctor.getEmail())).thenReturn(Optional.of(doctor));
        when(citasRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(noteRepository.save(any(ClinicalNote.class))).thenAnswer(invocation -> {
            ClinicalNote note = invocation.getArgument(0);
            if (note.getId() == null) note.setId(noteId);
            stored[0] = note;
            return note;
        });
        when(noteRepository.findById(noteId)).thenAnswer(invocation -> Optional.of(stored[0]));
        ClinicalNoteRequestDTO createRequest = new ClinicalNoteRequestDTO();
        createRequest.setAppointmentId(appointmentId);
        createRequest.setBody("Evaluación inicial");

        var draft = service.create(createRequest, doctor.getEmail());
        var finalNote = service.finalize(noteId, doctor.getEmail());
        ClinicalNoteAmendmentRequestDTO amendmentRequest = new ClinicalNoteAmendmentRequestDTO();
        amendmentRequest.setBody("Corrección clínica");
        amendmentRequest.setReason("Se corrigió una medida");
        ClinicalNote finalEntity = ClinicalNote.builder()
                .id(noteId).patientId(appointment.getUser().getId()).appointmentId(appointmentId)
                .authorId(doctor.getId()).status(ClinicalNoteStatus.FINAL).body("Evaluación inicial")
                .build();
        when(noteRepository.findById(noteId)).thenReturn(Optional.of(finalEntity));

        var amendment = service.amend(noteId, amendmentRequest, doctor.getEmail());

        assertThat(draft.getStatus()).isEqualTo(ClinicalNoteStatus.DRAFT);
        assertThat(finalNote.getStatus()).isEqualTo(ClinicalNoteStatus.FINAL);
        assertThat(amendment.getStatus()).isEqualTo(ClinicalNoteStatus.AMENDED);
        assertThat(amendment.getAmendedFromId()).isEqualTo(noteId);
    }

    @Test
    void rejectsDraftCreationBeforeAppointmentIsCompleted() {
        UUID appointmentId = UUID.randomUUID();
        User doctor = User.builder().id(UUID.randomUUID()).email("doctor@example.com").role(Rol.DOCTOR).build();
        when(userRepository.findByEmail(doctor.getEmail())).thenReturn(Optional.of(doctor));
        when(citasRepository.findById(appointmentId)).thenReturn(Optional.of(
                appointment(appointmentId, doctor, AppointmentStatus.CONFIRMADA)));
        ClinicalNoteRequestDTO request = new ClinicalNoteRequestDTO();
        request.setAppointmentId(appointmentId);
        request.setBody("Nota");

        assertThatThrownBy(() -> service.create(request, doctor.getEmail()))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.BusinessException.class)
                .hasMessageContaining("completar");
    }

    private Citas appointment(UUID id, User doctor, AppointmentStatus status) {
        return Citas.builder().id(id).user(User.builder().id(UUID.randomUUID()).build())
                .dentist(Dentist.builder().id(UUID.randomUUID()).user(doctor).Name("Doctor").build())
                .status(status).build();
    }
}
