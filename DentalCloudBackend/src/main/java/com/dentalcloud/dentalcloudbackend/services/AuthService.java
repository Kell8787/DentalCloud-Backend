package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.AccountActivationRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.AuthRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.AuthResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.RegisterPatientRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UserResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.ContactoEmergencia;
import com.dentalcloud.dentalcloudbackend.domain.entity.InformacionMedica;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Genero;
import com.dentalcloud.dentalcloudbackend.domain.enums.Parentesco;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.exceptions.ConflictException;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.repositories.ContactoEmergenciaRepository;
import com.dentalcloud.dentalcloudbackend.repositories.InformacionMedicaRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import com.dentalcloud.dentalcloudbackend.security.JwtService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

/*
    Servicio para manejar la autenticación y registro de usuarios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final ContactoEmergenciaRepository contactoEmergenciaRepository;
    private final InformacionMedicaRepository informacionMedicaRepository;

    /*
        Registra un nuevo usuario en el sistema.
     */
    @Transactional
    public String register(RegisterPatientRequestDTO request) {
        // Verificar si el usuario ya existe
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw pendingActivationOrDuplicate(existing, "El email ya está registrado");
        });

        userRepository.findByDui(request.getDui()).ifPresent(existing -> {
            throw pendingActivationOrDuplicate(existing, "El DUI ya está registrado");
        });

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new EntityExistsException("El número de teléfono ya está registrado");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        // Crear nuevo usuario
        User user = User.builder()
                .firstName(request.getFirstName())
                .secondName(request.getSecondName())
                .lastName(request.getLastName())
                .secondLastName(request.getSecondLastName())
                .genero(Genero.valueOf(request.getGenero().toUpperCase()))
                .dui(request.getDui())
                .birthDate(request.getBirthDate())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(Rol.CUSTOMER)
                .direccion(request.getDireccion())
                .build();

        User savedUser = userRepository.save(user);

        ContactoEmergencia contactoEmergencia = ContactoEmergencia.builder()
                .user(savedUser)
                .nombreCompleto(request.getContactoEmergencia().getNombreCompleto())
                .email(request.getContactoEmergencia().getEmail())
                .phoneNumber(request.getContactoEmergencia().getPhoneNumber())
                .parentesco(request.getContactoEmergencia().getParentesco())
                .build();

        contactoEmergenciaRepository.save(contactoEmergencia);

        InformacionMedica informacionMedica = InformacionMedica.builder()
                .user(savedUser)
                .alergias(request.getInformacionMedica().getAlergias())
                .medicamentos(request.getInformacionMedica().getMedicamentos())
                .antecedentesMedicos(request.getInformacionMedica().getAntecedentesMedicos())
                .build();

        informacionMedicaRepository.save(informacionMedica);

        return "Usuario registrado exitosamente";
    }

    /*
        Autentica a un usuario y genera un token JWT.
        Si las credenciales son inválidas, lanza una excepción.
     */
    public AuthResponseDTO login(AuthRequestDTO request) {
        log.info("Buscando usuario con email: {}", request.getEmail());

        // Buscar usuario por email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: {}", request.getEmail());
                    return new BadCredentialsException("Credenciales inválidas");
                });

        log.info("Usuario encontrado: {}", user.getEmail());

        // Verificar contraseña usando AuthenticationManager
        try {
            var auth = new UsernamePasswordAuthenticationToken(
                    request.getEmail(), request.getPassword()
            );
            authManager.authenticate(auth);
        } catch (Exception e) {
            log.warn("Contraseña incorrecta para: {}", request.getEmail());
            throw new BadCredentialsException("Credenciales inválidas");
        }

        // Generar token JWT
        String token = jwtService.generateToken(user.getEmail());
        log.info("Token generado para: {}", user.getEmail());

        // Crear respuesta con UUID convertido a String si es necesario
        return AuthResponseDTO.builder()
                .token(token)
                .user(toUserResponse(user))
                .message("Login exitoso")
                .build();
    }

    /*
        Activa el acceso web de un paciente dado de alta previamente por secretaría
        (active=false) verificando DUI, email y fecha de nacimiento, y establece su
        contraseña por primera vez.
     */
    @Transactional
    public AuthResponseDTO activateAccount(AccountActivationRequestDTO request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        User user = userRepository.findByDui(request.getDui())
                .filter(u -> u.getRole() == Rol.CUSTOMER)
                .filter(u -> !u.isActive())
                .filter(u -> u.getEmail().equalsIgnoreCase(request.getEmail()))
                .filter(u -> u.getBirthDate().equals(request.getBirthDate()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No encontramos un expediente pendiente de activación con esos datos."));

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        User saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getEmail());
        log.info("Cuenta activada para: {}", saved.getEmail());

        return AuthResponseDTO.builder()
                .token(token)
                .user(toUserResponse(saved))
                .message("Cuenta activada exitosamente")
                .build();
    }

    /*
        Distingue entre un duplicado real y un expediente administrativo pendiente de
        activación, para guiar al paciente al flujo correcto en /signup.
     */
    private RuntimeException pendingActivationOrDuplicate(User existing, String duplicateMessage) {
        if (existing.getRole() == Rol.CUSTOMER && !existing.isActive()) {
            return new ConflictException("PATIENT_RECORD_EXISTS",
                    "Ya tienes un expediente registrado en la clínica. Activa tu cuenta para continuar.");
        }
        return new EntityExistsException(duplicateMessage);
    }

    @Transactional
    public UserResponseDTO currentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));
        return toUserResponse(user);
    }

    private UserResponseDTO toUserResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .secondName(user.getSecondName())
                .lastName(user.getLastName())
                .secondLastName(user.getSecondLastName())
                .direccion(user.getDireccion())
                .genero(String.valueOf(user.getGenero()))
                .dui(user.getDui())
                .birthDate(user.getBirthDate())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .active(user.isActive())
                .build();
    }

    /*
        Valida un token JWT.
     */
    public boolean validateToken(String token) {
        try {
            return jwtService.isTokenValid(token);
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }
}
