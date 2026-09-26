package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.ActualizarContactoEmergenciaRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ActualizarPerfilRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ContactoEmergenciaDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.InformacionMedicaDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UserResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.PatientAdminCreateRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.PatientAdminUpdateRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UserActivationRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.ContactoEmergencia;
import com.dentalcloud.dentalcloudbackend.domain.entity.InformacionMedica;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Genero;
import com.dentalcloud.dentalcloudbackend.domain.enums.Parentesco;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import java.util.List;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.exceptions.ConflictException;
import com.dentalcloud.dentalcloudbackend.repositories.ContactoEmergenciaRepository;
import com.dentalcloud.dentalcloudbackend.repositories.InformacionMedicaRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final UserRepository userRepository;
    private final ContactoEmergenciaRepository contactoEmergenciaRepository;
    private final InformacionMedicaRepository informacionMedicaRepository;
    private final PasswordEncoder passwordEncoder;

    // GET /patients/profile
    public UserResponseDTO obtenerPerfil(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return mapearUserAResponse(user);
    }

    @Transactional
    // PUT /patients/profile
    public UserResponseDTO actualizarPerfil(String email, ActualizarPerfilRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getSecondName() != null) user.setSecondName(request.getSecondName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getSecondLastName() != null) user.setSecondLastName(request.getSecondLastName());
        if (request.getDireccion() != null) user.setDireccion(request.getDireccion());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getGenero() != null) user.setGenero(Genero.valueOf(request.getGenero().toUpperCase()));
        if (request.getBirthDate() != null) user.setBirthDate(request.getBirthDate());

        User userActualizado = userRepository.save(user);
        return mapearUserAResponse(userActualizado);
    }

    // GET /patients/emergency-contact
    public ContactoEmergenciaDTO obtenerContactoEmergencia(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        ContactoEmergencia contacto = contactoEmergenciaRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Contacto de emergencia no encontrado"));

        return ContactoEmergenciaDTO.builder()
                .nombreCompleto(contacto.getNombreCompleto())
                .email(contacto.getEmail())
                .phoneNumber(contacto.getPhoneNumber())
                .parentesco(Parentesco.valueOf(String.valueOf(contacto.getParentesco())))
                .build();
    }

    @Transactional
    // PUT /patients/emergency-contact
    public void actualizarContactoEmergencia(String email, ActualizarContactoEmergenciaRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        ContactoEmergencia contacto = contactoEmergenciaRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Contacto de emergencia no encontrado"));

        if (request.getNombreCompleto() != null) contacto.setNombreCompleto(request.getNombreCompleto());
        if (request.getEmail() != null) contacto.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) contacto.setPhoneNumber(request.getPhoneNumber());
        if (request.getParentesco() != null) contacto.setParentesco(Parentesco.valueOf(request.getParentesco().toUpperCase()));

        contactoEmergenciaRepository.save(contacto);
    }

    // GET /patients/medical-info/email?email= (SECRETARIA, ADMIN)
    public InformacionMedicaDTO obtenerInformacionMedicaPorEmail(String patientEmail) {
        User user = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        InformacionMedica info = informacionMedicaRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Información médica no encontrada"));

        InformacionMedicaDTO dto = new InformacionMedicaDTO();
        dto.setAlergias(info.getAlergias());
        dto.setMedicamentos(info.getMedicamentos());
        dto.setAntecedentesMedicos(info.getAntecedentesMedicos());
        return dto;
    }

    public InformacionMedicaDTO obtenerInformacionMedica(UUID patientId) {
        User user = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
        InformacionMedica info = informacionMedicaRepository.findByUser(user).orElse(null);
        InformacionMedicaDTO dto = new InformacionMedicaDTO();
        dto.setAlergias(info == null || info.getAlergias() == null ? List.of() : info.getAlergias());
        dto.setMedicamentos(info == null || info.getMedicamentos() == null ? List.of() : info.getMedicamentos());
        dto.setAntecedentesMedicos(info == null ? null : info.getAntecedentesMedicos());
        return dto;
    }

    public UserResponseDTO obtenerPerfil(UUID patientId) {
        return mapearUserAResponse(userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado")));
    }

    @Transactional
    // PUT /patients/medical-info/email?email= (SECRETARIA, ADMIN)
    public InformacionMedicaDTO actualizarInformacionMedicaPorEmail(String patientEmail, InformacionMedicaDTO request) {
        User user = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        InformacionMedica info = informacionMedicaRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Información médica no encontrada"));

        if (request.getAlergias() != null) info.setAlergias(request.getAlergias());
        if (request.getMedicamentos() != null) info.setMedicamentos(request.getMedicamentos());
        if (request.getAntecedentesMedicos() != null) info.setAntecedentesMedicos(request.getAntecedentesMedicos());

        informacionMedicaRepository.save(info);

        InformacionMedicaDTO dto = new InformacionMedicaDTO();
        dto.setAlergias(info.getAlergias());
        dto.setMedicamentos(info.getMedicamentos());
        dto.setAntecedentesMedicos(info.getAntecedentesMedicos());
        return dto;
    }

    // GET /patients/medical-info
    public InformacionMedicaDTO obtenerInformacionMedica(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        InformacionMedica info = informacionMedicaRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Información médica no encontrada"));

        InformacionMedicaDTO dto = new InformacionMedicaDTO();
        dto.setAlergias(info.getAlergias());
        dto.setMedicamentos(info.getMedicamentos());
        dto.setAntecedentesMedicos(info.getAntecedentesMedicos());
        return dto;
    }

    @Transactional
    // PUT /patients/medical-info
    public InformacionMedicaDTO actualizarInformacionMedica(String email, InformacionMedicaDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        InformacionMedica info = informacionMedicaRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Información médica no encontrada"));

        if (request.getAlergias() != null) info.setAlergias(request.getAlergias());
        if (request.getMedicamentos() != null) info.setMedicamentos(request.getMedicamentos());
        if (request.getAntecedentesMedicos() != null) info.setAntecedentesMedicos(request.getAntecedentesMedicos());

        informacionMedicaRepository.save(info);

        InformacionMedicaDTO dto = new InformacionMedicaDTO();
        dto.setAlergias(info.getAlergias());
        dto.setMedicamentos(info.getMedicamentos());
        dto.setAntecedentesMedicos(info.getAntecedentesMedicos());
        return dto;
    }

    public List<UserResponseDTO> buscarPacientes(String search) {
        List<User> pacientes = userRepository.findByRole(Rol.CUSTOMER);
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            pacientes = pacientes.stream()
                    .filter(u -> (u.getFirstName() + " " + u.getLastName()).toLowerCase().contains(q)
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))
                            || (u.getDui() != null && u.getDui().contains(q))
                            || (u.getPhoneNumber() != null && u.getPhoneNumber().contains(q)))
                .toList();
        }
        return pacientes.stream().map(this::mapearUserAResponse).toList();
    }

    @Transactional
    public UserResponseDTO crearPacienteAdministrativo(PatientAdminCreateRequestDTO request) {
        assertUnique(request.getEmail(), request.getDui(), request.getPhoneNumber(), null);
        User patient = User.builder()
                .firstName(request.getFirstName()).secondName(request.getSecondName())
                .lastName(request.getLastName()).secondLastName(request.getSecondLastName())
                .direccion(request.getDireccion()).genero(Genero.valueOf(request.getGenero().toUpperCase()))
                .dui(request.getDui()).birthDate(request.getBirthDate()).email(request.getEmail())
                .phoneNumber(request.getPhoneNumber()).role(Rol.CUSTOMER)
                .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                .active(false).build();
        User saved = userRepository.save(patient);
        contactoEmergenciaRepository.save(ContactoEmergencia.builder().user(saved)
                .nombreCompleto(request.getEmergencyName()).email(request.getEmergencyEmail())
                .phoneNumber(request.getEmergencyPhone())
                .parentesco(Parentesco.valueOf(request.getEmergencyRelationship().toUpperCase())).build());
        informacionMedicaRepository.save(InformacionMedica.builder().user(saved)
                .alergias(List.of()).medicamentos(List.of()).build());
        return mapearUserAResponse(saved);
    }

    @Transactional
    public UserResponseDTO actualizarPacienteAdministrativo(java.util.UUID id,
                                                             PatientAdminUpdateRequestDTO request) {
        User patient = patient(id);
        assertUnique(request.getEmail(), request.getDui(), request.getPhoneNumber(), id);
        if (request.getFirstName() != null) patient.setFirstName(request.getFirstName());
        if (request.getSecondName() != null) patient.setSecondName(request.getSecondName());
        if (request.getLastName() != null) patient.setLastName(request.getLastName());
        if (request.getSecondLastName() != null) patient.setSecondLastName(request.getSecondLastName());
        if (request.getDireccion() != null) patient.setDireccion(request.getDireccion());
        if (request.getGenero() != null) patient.setGenero(Genero.valueOf(request.getGenero().toUpperCase()));
        if (request.getBirthDate() != null) patient.setBirthDate(request.getBirthDate());
        if (request.getEmail() != null) patient.setEmail(request.getEmail());
        if (request.getDui() != null) patient.setDui(request.getDui());
        if (request.getPhoneNumber() != null) patient.setPhoneNumber(request.getPhoneNumber());
        return mapearUserAResponse(userRepository.save(patient));
    }

    @Transactional
    public UserResponseDTO activarPaciente(java.util.UUID id, UserActivationRequestDTO request) {
        User patient = patient(id);
        patient.setActive(request.isActive());
        return mapearUserAResponse(userRepository.save(patient));
    }

    private User patient(java.util.UUID id) {
        User patient = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
        if (patient.getRole() != Rol.CUSTOMER) {
            throw new ResourceNotFoundException("Paciente no encontrado");
        }
        return patient;
    }

    private void assertUnique(String email, String dui, String phone, java.util.UUID currentId) {
        if (email != null && userRepository.findByEmail(email)
                .filter(user -> !user.getId().equals(currentId)).isPresent()) {
            throw new ConflictException("PATIENT_EMAIL_EXISTS", "El email ya está registrado");
        }
        if (dui != null && userRepository.findByDui(dui)
                .filter(user -> currentId == null || !user.getId().equals(currentId)).isPresent()) {
                throw new ConflictException("PATIENT_DUPLICATE", "El DUI ya está registrado");
        }
        if (phone != null && userRepository.findByPhoneNumber(phone)
                .filter(user -> currentId == null || !user.getId().equals(currentId)).isPresent()) {
            throw new ConflictException("PATIENT_DUPLICATE", "El teléfono ya está registrado");
        }
    }

    private UserResponseDTO mapearUserAResponse(User user) {
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
}
