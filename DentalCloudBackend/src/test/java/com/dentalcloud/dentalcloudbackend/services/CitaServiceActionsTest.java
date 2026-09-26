package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.entity.Citas;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.repositories.AppointmentStatusEventRepository;
import com.dentalcloud.dentalcloudbackend.repositories.CitasRepository;
import com.dentalcloud.dentalcloudbackend.repositories.ClinicScheduleRepository;
import com.dentalcloud.dentalcloudbackend.repositories.DentistRepository;
import com.dentalcloud.dentalcloudbackend.repositories.PatientTreatmentPlanRepository;
import com.dentalcloud.dentalcloudbackend.repositories.TratamientoRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitaServiceActionsTest {
    @Mock private CitasRepository citasRepository;
    @Mock private ClinicScheduleRepository clinicScheduleRepository;
    @Mock private UserRepository userRepository;
    @Mock private TratamientoRepository tratamientoRepository;
    @Mock private DentistRepository dentistRepository;
    @Mock private PatientTreatmentPlanRepository treatmentPlanRepository;
    @Mock private AppointmentStatusEventRepository appointmentStatusEventRepository;
    @InjectMocks private CitaService service;

    @Test
    void requiresReasonWhenStaffCancels() {
        User secretary = User.builder().id(UUID.randomUUID()).email("secretary@example.com")
                .role(Rol.SECRETARIA).build();
        Citas appointment = Citas.builder().id(UUID.randomUUID())
                .user(User.builder().id(UUID.randomUUID()).build())
                .status(AppointmentStatus.CONFIRMADA)
                .startsAt(LocalDateTime.now().plusDays(2)).build();
        when(citasRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail(secretary.getEmail())).thenReturn(Optional.of(secretary));

        assertThatThrownBy(() -> service.cancelarCita(appointment.getId(), secretary.getEmail(), null))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.BusinessException.class)
                .hasMessageContaining("motivo");
    }

    @Test
    void cannotCompleteAppointmentBeforeItEnds() {
        User doctor = User.builder().id(UUID.randomUUID()).email("doctor@example.com")
                .role(Rol.DOCTOR).build();
        Dentist dentist = Dentist.builder().id(UUID.randomUUID()).user(doctor).Name("Doctor").build();
        Citas appointment = Citas.builder().id(UUID.randomUUID())
                .user(User.builder().id(UUID.randomUUID()).build())
                .dentist(dentist)
                .status(AppointmentStatus.CONFIRMADA)
                .startsAt(LocalDateTime.now().minusMinutes(30))
                .endsAt(LocalDateTime.now().plusMinutes(30)).build();
        when(citasRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail(doctor.getEmail())).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> service.completarCita(appointment.getId(), doctor.getEmail()))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.BusinessException.class)
                .hasMessageContaining("hora de finalización");
    }

    @Test
    void doctorCannotCompleteAnotherDoctorsAppointment() {
        User doctor = User.builder().id(UUID.randomUUID()).email("doctor@example.com")
                .role(Rol.DOCTOR).build();
        User otherDoctor = User.builder().id(UUID.randomUUID()).email("other@example.com")
                .role(Rol.DOCTOR).build();
        Dentist dentist = Dentist.builder().id(UUID.randomUUID()).user(otherDoctor).Name("Otro doctor").build();
        Citas appointment = Citas.builder().id(UUID.randomUUID())
                .user(User.builder().id(UUID.randomUUID()).build())
                .dentist(dentist)
                .status(AppointmentStatus.CONFIRMADA)
                .startsAt(LocalDateTime.now().minusHours(2))
                .endsAt(LocalDateTime.now().minusHours(1)).build();
        when(citasRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
        when(userRepository.findByEmail(doctor.getEmail())).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> service.completarCita(appointment.getId(), doctor.getEmail()))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.BusinessException.class)
                .hasMessageContaining("otro doctor");
    }
}
