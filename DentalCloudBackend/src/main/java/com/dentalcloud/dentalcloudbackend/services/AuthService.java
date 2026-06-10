package com.dentalcloud.dentalcloudbackend.services;

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
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EntityExistsException("El email ya está registrado");
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
                    return new EntityNotFoundException("Credenciales inválidas");
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
            throw new EntityNotFoundException("Credenciales inválidas");
        }

        // Generar token JWT
        String token = jwtService.generateToken(user.getEmail());
        log.info("Token generado para: {}", user.getEmail());

        // Crear respuesta con UUID convertido a String si es necesario
        UserResponseDTO userResponse = UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .secondName(user.getSecondName())
                .lastName(user.getLastName())
                .secondLastName(user.getSecondLastName())
                .genero(String.valueOf(user.getGenero()))
                .dui(user.getDui())
                .role(user.getRole().name())
                .build();

        return AuthResponseDTO.builder()
                .token(token)
                .user(userResponse)
                .message("Login exitoso")
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
