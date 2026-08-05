package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.PatientAdminCreateRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.repositories.ContactoEmergenciaRepository;
import com.dentalcloud.dentalcloudbackend.repositories.InformacionMedicaRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceAdminTest {
    @Mock private UserRepository userRepository;
    @Mock private ContactoEmergenciaRepository contactoEmergenciaRepository;
    @Mock private InformacionMedicaRepository informacionMedicaRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private PatientService service;

    @Test
    void rejectsDuplicateAdministrativePatientEmail() {
        PatientAdminCreateRequestDTO request = new PatientAdminCreateRequestDTO();
        request.setEmail("existing@example.com");
        User existing = User.builder().id(java.util.UUID.randomUUID()).email(request.getEmail())
                .role(Rol.CUSTOMER).build();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.crearPacienteAdministrativo(request))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.ConflictException.class)
                .hasMessageContaining("email");
    }
}
