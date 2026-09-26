package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.AppointmentRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.StaffAppointmentRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.ClinicSchedule;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.PatientTreatmentPlan;
import com.dentalcloud.dentalcloudbackend.domain.entity.Tratamiento;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentPlanStatus;
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

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/El_Salvador");
    @Mock private CitasRepository citasRepository;
    @Mock private ClinicScheduleRepository clinicScheduleRepository;
    @Mock private UserRepository userRepository;
    @Mock private TratamientoRepository tratamientoRepository;
    @Mock private DentistRepository dentistRepository;
    @Mock private PatientTreatmentPlanRepository treatmentPlanRepository;
    @Mock private AppointmentStatusEventRepository appointmentStatusEventRepository;
    @InjectMocks private CitaService service;

    @Test
    void patientCanRequestAFirstAppointmentWithoutAClinicalPlan() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID treatmentId = UUID.randomUUID();
        LocalDate date = LocalDate.now(BUSINESS_ZONE).plusDays(7);
        LocalDateTime startsAt = LocalDateTime.of(date, LocalTime.of(9, 0));
        User patient = User.builder().id(patientId).firstName("Ana").lastName("Paciente")
                .email("patient@example.com").role(Rol.CUSTOMER).build();
        Dentist dentist = Dentist.builder().id(doctorId).Name("Dr. Demo").build();
        Tratamiento treatment = Tratamiento.builder().id(treatmentId).nombre("Limpieza")
                .duracionMinutos(30).active(true).build();
        ClinicSchedule schedule = ClinicSchedule.builder()
                .dayOfWeek(date.getDayOfWeek().getValue())
                .opensAt(LocalTime.of(8, 0)).closesAt(LocalTime.of(17, 0)).enabled(true).build();
        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setDoctorId(doctorId);
        request.setTreatmentId(treatmentId);
        request.setStartsAt(startsAt);
        request.setReason("Primera valoración");

        when(userRepository.findByEmail(patient.getEmail())).thenReturn(Optional.of(patient));
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(dentistRepository.findByIdForUpdate(doctorId)).thenReturn(Optional.of(dentist));
        when(tratamientoRepository.findByIdAndActiveTrue(treatmentId)).thenReturn(Optional.of(treatment));
        when(clinicScheduleRepository.findByDayOfWeek(date.getDayOfWeek().getValue())).thenReturn(Optional.of(schedule));
        when(citasRepository.save(org.mockito.ArgumentMatchers.any(com.dentalcloud.dentalcloudbackend.domain.entity.Citas.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.solicitarCita(request, patient.getEmail());

        assertThat(response.getStatus()).isEqualTo(com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus.SOLICITADA);
        assertThat(response.getTreatmentPlanId()).isNull();
        assertThat(response.getTratamientoId()).isEqualTo(treatmentId);
        assertThat(response.getDentistaId()).isEqualTo(doctorId);
    }

    @Test
    void rejectsAppointmentWhenPlanDoesNotMatchPatientDoctorOrTreatment() {
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID treatmentId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        User patient = User.builder().id(patientId).role(Rol.CUSTOMER).build();
        Dentist dentist = Dentist.builder().id(doctorId).build();
        Tratamiento treatment = Tratamiento.builder().id(treatmentId).duracionMinutos(60).active(true).build();
        PatientTreatmentPlan plan = PatientTreatmentPlan.builder()
                .id(planId).patientId(UUID.randomUUID()).dentistId(doctorId).treatmentId(treatmentId)
                .status(TreatmentPlanStatus.ACTIVE).build();
        StaffAppointmentRequestDTO request = new StaffAppointmentRequestDTO();
        request.setPatientId(patientId);
        request.setDoctorId(doctorId);
        request.setTreatmentId(treatmentId);
        request.setTreatmentPlanId(planId);

        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(dentistRepository.findByIdForUpdate(doctorId)).thenReturn(Optional.of(dentist));
        when(tratamientoRepository.findByIdAndActiveTrue(treatmentId)).thenReturn(Optional.of(treatment));
        when(treatmentPlanRepository.findById(planId)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> service.crearCitaStaff(request))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.BusinessException.class)
                .hasMessageContaining("no coincide");
    }
}
