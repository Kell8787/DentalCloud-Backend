package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.entity.PatientTreatmentPlan;
import com.dentalcloud.dentalcloudbackend.domain.entity.TreatmentStep;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentStepStatus;
import com.dentalcloud.dentalcloudbackend.repositories.DentistRepository;
import com.dentalcloud.dentalcloudbackend.repositories.PatientTreatmentPlanRepository;
import com.dentalcloud.dentalcloudbackend.repositories.TreatmentStepRepository;
import com.dentalcloud.dentalcloudbackend.repositories.TratamientoRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreatmentPlanServiceTest {
    @Mock
    private PatientTreatmentPlanRepository planRepository;
    @Mock
    private TreatmentStepRepository stepRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DentistRepository dentistRepository;
    @Mock
    private TratamientoRepository tratamientoRepository;
    @InjectMocks
    private TreatmentPlanService service;

    @Test
    void calculatesProgressFromCompletedStepsOnly() {
        UUID patientId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        User patient = User.builder().id(patientId).email("patient@example.com").role(Rol.CUSTOMER).build();
        PatientTreatmentPlan plan = PatientTreatmentPlan.builder()
                .id(planId).patientId(patientId).treatmentId(UUID.randomUUID())
                .dentistId(UUID.randomUUID()).version(0L).build();
        List<TreatmentStep> steps = List.of(
                TreatmentStep.builder().id(UUID.randomUUID()).planId(planId).title("Consulta")
                        .position(1).status(TreatmentStepStatus.COMPLETED).version(0L).build(),
                TreatmentStep.builder().id(UUID.randomUUID()).planId(planId).title("Control")
                        .position(2).status(TreatmentStepStatus.PENDING).version(0L).build()
        );
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(userRepository.findByEmail("patient@example.com")).thenReturn(Optional.of(patient));
        when(stepRepository.findByPlanIdOrderByPositionAsc(planId)).thenReturn(steps);

        var response = service.get(planId, "patient@example.com");

        assertThat(response.getProgress().getCompletedSteps()).isEqualTo(1);
        assertThat(response.getProgress().getTotalSteps()).isEqualTo(2);
        assertThat(response.getProgress().getPercentage()).isEqualByComparingTo("50.00");
    }

    @Test
    void hidesAnotherPatientsPlan() {
        UUID patientId = UUID.randomUUID();
        PatientTreatmentPlan plan = PatientTreatmentPlan.builder()
                .id(UUID.randomUUID()).patientId(patientId).treatmentId(UUID.randomUUID())
                .dentistId(UUID.randomUUID()).build();
        User otherPatient = User.builder().id(UUID.randomUUID()).email("other@example.com")
                .role(Rol.CUSTOMER).build();
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherPatient));

        assertThatThrownBy(() -> service.get(plan.getId(), "other@example.com"))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException.class);
    }
}
