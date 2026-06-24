package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.CambiarRolRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.DoctorResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UserResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.repositories.DentistRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DentistRepository dentistRepository;

    public List<DoctorResponseDTO> obtenerDoctores() {
        return userRepository.findByRole(Rol.DOCTOR).stream()
                .map(user -> {
                    UUID dentistId = dentistRepository.findByUser(user)
                            .map(Dentist::getId)
                            .orElse(null);
                    return DoctorResponseDTO.builder()
                            .id(user.getId())
                            .dentistId(dentistId)
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .phoneNumber(user.getPhoneNumber())
                            .build();
                })
                .toList();
    }

    @Transactional
    public UserResponseDTO cambiarRol(UUID userId, CambiarRolRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setRole(request.getNuevoRol());
        userRepository.save(user);

        if (request.getNuevoRol() == Rol.DOCTOR) {
            boolean yaExisteDentist = dentistRepository.findByUser(user).isPresent();
            if (!yaExisteDentist) {
                Dentist dentist = Dentist.builder()
                        .Name(user.getFirstName() + " " + user.getLastName())
                        .user(user)
                        .build();
                dentistRepository.save(dentist);
            }
        }

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
