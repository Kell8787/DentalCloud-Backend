package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.StaffAppointmentRequestDTO;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {
    @Mock private CitasRepository citasRepository;
    @Mock private ClinicScheduleRepository clinicScheduleRepository;
    @Mock private UserRepository userRepository;
    @Mock private TratamientoRepository tratamientoRepository;
    @Mock private DentistRepository dentistRepository;
    @Mock private PatientTreatmentPlanRepository treatmentPlanRepository;
    @Mock private AppointmentStatusEventRepository appointmentStatusEventRepository;
    @InjectMocks private CitaService service;

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
