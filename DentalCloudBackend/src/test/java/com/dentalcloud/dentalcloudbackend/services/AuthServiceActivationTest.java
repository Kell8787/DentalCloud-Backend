package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.AccountActivationRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.RegisterPatientRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.exceptions.ConflictException;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.repositories.ContactoEmergenciaRepository;
import com.dentalcloud.dentalcloudbackend.repositories.InformacionMedicaRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import com.dentalcloud.dentalcloudbackend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceActivationTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authManager;
    @Mock private ContactoEmergenciaRepository contactoEmergenciaRepository;
    @Mock private InformacionMedicaRepository informacionMedicaRepository;
    @InjectMocks private AuthService authService;

    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 15);

    private User pendingPatient() {
        return User.builder()
                .id(UUID.randomUUID())
                .dui("12345678-9")
                .email("ana@example.com")
                .birthDate(BIRTH_DATE)
                .role(Rol.CUSTOMER)
                .active(false)
                .password("encoded-random-uuid")
                .build();
    }

    private AccountActivationRequestDTO validRequest() {
        return AccountActivationRequestDTO.builder()
                .dui("12345678-9")
                .email("ana@example.com")
                .birthDate(BIRTH_DATE)
                .password("new-password")
                .confirmPassword("new-password")
                .build();
    }

    @Test
    void activatesPendingPatientAndReturnsToken() {
        User pending = pendingPatient();
        when(userRepository.findByDui("12345678-9")).thenReturn(Optional.of(pending));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken("ana@example.com")).thenReturn("jwt-token");

        var response = authService.activateAccount(validRequest());

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(pending.isActive()).isTrue();
        assertThat(pending.getPassword()).isEqualTo("encoded-new-password");
    }

    @Test
    void rejectsMismatchedPasswords() {
        AccountActivationRequestDTO request = validRequest();
        request.setConfirmPassword("different");

        assertThatThrownBy(() -> authService.activateAccount(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsWhenEmailDoesNotMatch() {
        User pending = pendingPatient();
        when(userRepository.findByDui("12345678-9")).thenReturn(Optional.of(pending));

        AccountActivationRequestDTO request = validRequest();
        request.setEmail("otro@example.com");

        assertThatThrownBy(() -> authService.activateAccount(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsWhenAccountAlreadyActive() {
        User active = pendingPatient();
        active.setActive(true);
        when(userRepository.findByDui("12345678-9")).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> authService.activateAccount(validRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void registerRejectsSelfSignupWhenPendingRecordExistsByEmail() {
        User pending = pendingPatient();
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(pending));

        RegisterPatientRequestDTO request = new RegisterPatientRequestDTO();
        request.setEmail("ana@example.com");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("expediente");
    }
}
