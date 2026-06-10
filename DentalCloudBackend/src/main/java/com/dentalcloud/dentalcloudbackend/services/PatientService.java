package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.ActualizarContactoEmergenciaRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ActualizarPerfilRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ContactoEmergenciaDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.InformacionMedicaDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UserResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.ContactoEmergencia;
import com.dentalcloud.dentalcloudbackend.domain.entity.InformacionMedica;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Genero;
import com.dentalcloud.dentalcloudbackend.domain.enums.Parentesco;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.repositories.ContactoEmergenciaRepository;
import com.dentalcloud.dentalcloudbackend.repositories.InformacionMedicaRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final UserRepository userRepository;
    private final ContactoEmergenciaRepository contactoEmergenciaRepository;
    private final InformacionMedicaRepository informacionMedicaRepository;

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
                .build();
    }
}
