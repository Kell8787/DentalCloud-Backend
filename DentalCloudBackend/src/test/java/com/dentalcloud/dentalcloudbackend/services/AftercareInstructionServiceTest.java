package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.AftercareInstructionRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.AftercareInstruction;
import com.dentalcloud.dentalcloudbackend.domain.entity.Citas;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.repositories.AftercareInstructionRepository;
import com.dentalcloud.dentalcloudbackend.repositories.CitasRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AftercareInstructionServiceTest {
    @Mock private AftercareInstructionRepository instructionRepository;
    @Mock private CitasRepository citasRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private AftercareInstructionService service;

    @Test
    void hidesAnotherDoctorsAppointmentWhenCreatingInstruction() {
        UUID doctorId = UUID.randomUUID();
        UUID otherDoctorId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        User actor = User.builder().id(doctorId).email("doctor@example.com").role(Rol.DOCTOR).build();
        User otherDoctor = User.builder().id(otherDoctorId).build();
        Dentist dentist = Dentist.builder().id(UUID.randomUUID()).user(otherDoctor).build();
        Citas appointment = Citas.builder().id(appointmentId).dentist(dentist)
                .user(User.builder().id(UUID.randomUUID()).build())
                .status(AppointmentStatus.COMPLETADA).build();
        AftercareInstructionRequestDTO request = new AftercareInstructionRequestDTO();
        request.setAppointmentId(appointmentId);
        request.setTitle("Cuidados");
        request.setBody("No fumar durante 24 horas");

        when(userRepository.findByEmail(actor.getEmail())).thenReturn(Optional.of(actor));
        when(citasRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> service.create(request, actor.getEmail()))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException.class);
    }
}
