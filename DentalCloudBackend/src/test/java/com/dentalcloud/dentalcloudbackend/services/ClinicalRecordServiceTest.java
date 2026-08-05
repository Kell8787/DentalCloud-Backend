package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.CitaResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.InformacionMedicaDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UserResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicalRecordServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PatientService patientService;
    @Mock private CitaService citaService;
    @Mock private TreatmentPlanService treatmentPlanService;
    @Mock private AftercareInstructionService aftercareInstructionService;
    @Mock private ClinicalDocumentService clinicalDocumentService;
    @InjectMocks private ClinicalRecordService service;

    @Test
    void aggregatesOnlyTheDoctorScopedClinicalData() {
        UUID patientId = UUID.randomUUID();
        User patient = User.builder().id(patientId).role(Rol.CUSTOMER).build();
        User doctor = User.builder().id(UUID.randomUUID()).email("doctor@example.com").role(Rol.DOCTOR).build();
        CitaResponseDTO appointment = CitaResponseDTO.builder().id(UUID.randomUUID()).build();
        when(userRepository.findByEmail(doctor.getEmail())).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(citaService.obtenerCitasDeExpediente(patientId, doctor.getEmail())).thenReturn(List.of(appointment));
        when(treatmentPlanService.listForPatient(patientId, doctor.getEmail())).thenReturn(List.of());
        when(aftercareInstructionService.listForClinicalRecord(patientId, doctor.getEmail())).thenReturn(List.of());
        when(clinicalDocumentService.listForClinicalRecord(patientId, doctor.getEmail())).thenReturn(List.of());
        when(patientService.obtenerPerfil(patientId)).thenReturn(UserResponseDTO.builder().id(patientId).build());
        when(patientService.obtenerInformacionMedica(patientId)).thenReturn(new InformacionMedicaDTO());

        var response = service.get(patientId, doctor.getEmail());

        assertThat(response.getPatientId()).isEqualTo(patientId);
        assertThat(response.getAppointments()).containsExactly(appointment);
        assertThat(response.getPlans()).isEmpty();
    }

    @Test
    void hidesAnUnrelatedPatientFromDoctor() {
        UUID patientId = UUID.randomUUID();
        User patient = User.builder().id(patientId).role(Rol.CUSTOMER).build();
        User doctor = User.builder().id(UUID.randomUUID()).email("doctor@example.com").role(Rol.DOCTOR).build();
        when(userRepository.findByEmail(doctor.getEmail())).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(citaService.obtenerCitasDeExpediente(eq(patientId), eq(doctor.getEmail()))).thenReturn(List.of());
        when(treatmentPlanService.listForPatient(patientId, doctor.getEmail())).thenReturn(List.of());

        assertThatThrownBy(() -> service.get(patientId, doctor.getEmail()))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException.class);
    }
}
