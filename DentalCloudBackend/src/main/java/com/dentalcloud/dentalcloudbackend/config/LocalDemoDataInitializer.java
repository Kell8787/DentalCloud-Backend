package com.dentalcloud.dentalcloudbackend.config;

import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.PatientTreatmentPlan;
import com.dentalcloud.dentalcloudbackend.domain.entity.Tratamiento;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Genero;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentPlanStatus;
import com.dentalcloud.dentalcloudbackend.repositories.DentistRepository;
import com.dentalcloud.dentalcloudbackend.repositories.PatientTreatmentPlanRepository;
import com.dentalcloud.dentalcloudbackend.repositories.TratamientoRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;

@Component
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class LocalDemoDataInitializer implements CommandLineRunner {
    private static final String PATIENT_EMAIL = "demo.patient@dentalcloud.local";
    private static final String SECRETARY_EMAIL = "demo.secretaria@dentalcloud.local";
    private static final String TREATMENT_NAME = "Limpieza dental de seguimiento";

    private final UserRepository userRepository;
    private final DentistRepository dentistRepository;
    private final TratamientoRepository treatmentRepository;
    private final PatientTreatmentPlanRepository planRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User patient = ensurePatient();
        ensureSecretary();
        Tratamiento treatment = ensureTreatment();
        Dentist dentist = dentistRepository.findByUserEmail("doctor1@dentalcloud.com")
                .orElseThrow(() -> new IllegalStateException("Falta el doctor demo doctor1@dentalcloud.com"));

        boolean hasActivePlan = planRepository.findByPatientIdAndStatusOrderByCreatedAtDesc(
                patient.getId(), TreatmentPlanStatus.ACTIVE).stream()
                .anyMatch(plan -> plan.getTreatmentId().equals(treatment.getId()));
        if (!hasActivePlan) {
            planRepository.save(PatientTreatmentPlan.builder()
                    .patientId(patient.getId())
                    .treatmentId(treatment.getId())
                    .dentistId(dentist.getId())
                    .status(TreatmentPlanStatus.ACTIVE)
                    .startedAt(Instant.now())
                    .version(0L)
                    .build());
            log.info("Plan demo activo creado para {}", PATIENT_EMAIL);
        }
    }

    private User ensurePatient() {
        return userRepository.findByEmail(PATIENT_EMAIL).orElseGet(() -> userRepository.save(User.builder()
                .firstName("Paciente")
                .lastName("Demo")
                .direccion("San Salvador, El Salvador")
                .genero(Genero.FEMENINO)
                .dui("99000000-1")
                .birthDate(LocalDate.of(1995, 5, 10))
                .email(PATIENT_EMAIL)
                .password(passwordEncoder.encode("DemoPatient123!"))
                .phoneNumber("7999-0001")
                .role(Rol.CUSTOMER)
                .build()));
    }

    private void ensureSecretary() {
        if (userRepository.findByEmail(SECRETARY_EMAIL).isEmpty()) {
            userRepository.save(User.builder()
                    .firstName("Secretaría")
                    .lastName("Demo")
                    .direccion("San Salvador, El Salvador")
                    .genero(Genero.FEMENINO)
                    .dui("99000000-2")
                    .birthDate(LocalDate.of(1992, 8, 20))
                    .email(SECRETARY_EMAIL)
                    .password(passwordEncoder.encode("DemoSecretary123!"))
                    .phoneNumber("7999-0002")
                    .role(Rol.SECRETARIA)
                    .build());
        }
    }

    private Tratamiento ensureTreatment() {
        return treatmentRepository.findByNombre(TREATMENT_NAME).orElseGet(() -> treatmentRepository.save(Tratamiento.builder()
                .nombre(TREATMENT_NAME)
                .descripcion("Tratamiento demo para validar selección y solicitud de citas.")
                .duracionMinutos(45)
                .precio(java.math.BigDecimal.valueOf(35))
                .active(true)
                .build()));
    }
}
