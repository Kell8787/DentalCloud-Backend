package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.dentalcloud.dentalcloudbackend.domain.entity.InformacionMedica;
import com.dentalcloud.dentalcloudbackend.domain.enums.Parentesco;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterPatientRequestDTO {
    @NotBlank
    private String firstName;

    private String secondName;

    @NotBlank
    private String lastName;

    private String secondLastName;

    @NotNull
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate birthDate;

    @NotBlank
    private String genero;

    @NotBlank
    private String dui;

    @NotBlank
    private String phoneNumber;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;

    @NotBlank
    private String direccion;

    private ContactoEmergenciaDTO contactoEmergencia;

    private InformacionMedicaDTO informacionMedica;
}
