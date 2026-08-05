package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientAdminUpdateRequestDTO {
    private String firstName;
    private String secondName;
    private String lastName;
    private String secondLastName;
    private String direccion;
    private String genero;
    private String dui;
    private LocalDate birthDate;
    @Email private String email;
    private String phoneNumber;
}
