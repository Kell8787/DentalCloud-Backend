package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientAdminCreateRequestDTO {
    @NotBlank private String firstName;
    private String secondName;
    @NotBlank private String lastName;
    private String secondLastName;
    @NotBlank private String direccion;
    @NotBlank @Pattern(regexp = "MASCULINO|FEMENINO") private String genero;
    @NotBlank private String dui;
    @NotNull @Past private LocalDate birthDate;
    @NotBlank @Email private String email;
    @NotBlank @Pattern(regexp = "^[0-9]{4}-[0-9]{4}$") private String phoneNumber;
    @NotBlank private String emergencyName;
    @NotBlank @Email private String emergencyEmail;
    @NotBlank @Pattern(regexp = "^[0-9]{4}-[0-9]{4}$") private String emergencyPhone;
    @NotBlank private String emergencyRelationship;
}
