package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.entity.PatientTreatmentPlan;
import com.dentalcloud.dentalcloudbackend.domain.entity.TreatmentStep;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.Tratamiento;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.dto.CreateTreatmentPlanRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentPlanStatus;
import com.dentalcloud.dentalcloudbackend.domain.dto.UpdateTreatmentStepRequestDTO;
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
import java.math.BigDecimal;

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
    void doctorCanCreateAndActivateAPlanForAPatient() {
        UUID patientId = UUID.randomUUID();
        UUID treatmentId = UUID.randomUUID();
        UUID dentistId = UUID.randomUUID();
        User doctor = User.builder().id(UUID.randomUUID()).email("doctor@example.com").role(Rol.DOCTOR).build();
        User patient = User.builder().id(patientId).email("patient@example.com").role(Rol.CUSTOMER).build();
        Dentist dentist = Dentist.builder().id(dentistId).Name("Dr. Demo").user(doctor).build();
        Tratamiento treatment = Tratamiento.builder().id(treatmentId).nombre("Limpieza").duracionMinutos(30)
                .precio(BigDecimal.TEN).active(true).build();

        when(userRepository.findByEmail(doctor.getEmail())).thenReturn(Optional.of(doctor));
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(dentistRepository.findById(dentistId)).thenReturn(Optional.of(dentist));
        when(tratamientoRepository.findByIdAndActiveTrue(treatmentId)).thenReturn(Optional.of(treatment));
        when(planRepository.save(org.mockito.ArgumentMatchers.any(PatientTreatmentPlan.class)))
                .thenAnswer(invocation -> {
                    PatientTreatmentPlan saved = invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    return saved;
                });
        when(tratamientoRepository.findById(treatmentId)).thenReturn(Optional.of(treatment));
        when(stepRepository.findByPlanIdOrderByPositionAsc(org.mockito.ArgumentMatchers.any(UUID.class)))
                .thenReturn(List.of());

        CreateTreatmentPlanRequestDTO request = new CreateTreatmentPlanRequestDTO();
        request.setTreatmentId(treatmentId);
        request.setDentistId(dentistId);

        var created = service.create(patientId, request, doctor.getEmail());

        assertThat(created.getStatus()).isEqualTo(TreatmentPlanStatus.PLANNED);
        assertThat(created.getPatientId()).isEqualTo(patientId);
        assertThat(created.getDentistId()).isEqualTo(dentistId);

        when(planRepository.findById(created.getId())).thenReturn(Optional.of(
                PatientTreatmentPlan.builder().id(created.getId()).patientId(patientId).treatmentId(treatmentId)
                        .dentistId(dentistId).status(TreatmentPlanStatus.PLANNED).version(0L).build()));
        when(planRepository.save(org.mockito.ArgumentMatchers.any(PatientTreatmentPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var activation = new com.dentalcloud.dentalcloudbackend.domain.dto.UpdateTreatmentPlanRequestDTO();
        activation.setStatus(TreatmentPlanStatus.ACTIVE);
        var activated = service.update(created.getId(), activation, doctor.getEmail());

        assertThat(activated.getStatus()).isEqualTo(TreatmentPlanStatus.ACTIVE);
    }

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

    @Test
    void returnsUpdatedProgressWhenCompletingStep() {
        UUID planId = UUID.randomUUID();
        UUID stepId = UUID.randomUUID();
        User admin = User.builder().id(UUID.randomUUID()).email("admin@example.com").role(Rol.ADMIN).build();
        PatientTreatmentPlan plan = PatientTreatmentPlan.builder().id(planId).patientId(UUID.randomUUID())
                .dentistId(UUID.randomUUID()).status(TreatmentPlanStatus.ACTIVE).version(0L).build();
        TreatmentStep step = TreatmentStep.builder().id(stepId).planId(planId).title("Consulta")
                .position(1).status(TreatmentStepStatus.PENDING).version(0L).build();
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(step));
        when(stepRepository.save(step)).thenReturn(step);
        when(stepRepository.findByPlanIdOrderByPositionAsc(planId)).thenReturn(List.of(step));
        UpdateTreatmentStepRequestDTO request = new UpdateTreatmentStepRequestDTO();
        request.setStatus(TreatmentStepStatus.COMPLETED);

        var response = service.updateStep(planId, stepId, request, admin.getEmail());

        assertThat(response.getStatus()).isEqualTo(TreatmentStepStatus.COMPLETED);
        assertThat(response.getProgress().getCompletedSteps()).isEqualTo(1);
        assertThat(response.getProgress().getPercentage()).isEqualByComparingTo("100.00");
    }

    @Test
    void rejectsStepChangesAfterPlanIsClosed() {
        UUID planId = UUID.randomUUID();
        User admin = User.builder().id(UUID.randomUUID()).email("admin@example.com").role(Rol.ADMIN).build();
        PatientTreatmentPlan plan = PatientTreatmentPlan.builder().id(planId).patientId(UUID.randomUUID())
                .dentistId(UUID.randomUUID()).status(TreatmentPlanStatus.COMPLETED).version(0L).build();
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.updateStep(planId, UUID.randomUUID(),
                new UpdateTreatmentStepRequestDTO(), admin.getEmail()))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.BusinessException.class)
                .hasMessageContaining("plan cerrado");
    }

    @Test
    void secretariaCannotEditClinicalSteps() {
        UUID planId = UUID.randomUUID();
        User secretary = User.builder().id(UUID.randomUUID()).email("secretaria@example.com")
                .role(Rol.SECRETARIA).build();
        PatientTreatmentPlan plan = PatientTreatmentPlan.builder().id(planId).patientId(UUID.randomUUID())
                .dentistId(UUID.randomUUID()).status(TreatmentPlanStatus.ACTIVE).version(0L).build();
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(userRepository.findByEmail(secretary.getEmail())).thenReturn(Optional.of(secretary));

        assertThatThrownBy(() -> service.addStep(planId,
                new com.dentalcloud.dentalcloudbackend.domain.dto.CreateTreatmentStepRequestDTO(), secretary.getEmail()))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.BusinessException.class)
                .hasMessageContaining("doctor o administrador");
    }

    @Test
    void secretariaCannotCreateAPlanForAPatient() {
        UUID patientId = UUID.randomUUID();
        User secretary = User.builder().id(UUID.randomUUID()).email("secretaria@example.com")
                .role(Rol.SECRETARIA).build();
        when(userRepository.findByEmail(secretary.getEmail())).thenReturn(Optional.of(secretary));

        assertThatThrownBy(() -> service.create(patientId, new CreateTreatmentPlanRequestDTO(), secretary.getEmail()))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.BusinessException.class)
                .hasMessageContaining("doctor o administrador");
    }
}
