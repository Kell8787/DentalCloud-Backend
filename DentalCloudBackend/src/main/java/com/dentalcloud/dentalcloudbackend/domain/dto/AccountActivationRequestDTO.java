package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountActivationRequestDTO {

    @NotBlank(message = "El DUI es requerido.")
    private String dui;

    @NotBlank(message = "El correo electrónico es requerido.")
    @Email(message = "Formato de correo inválido.")
    private String email;

    @NotNull(message = "La fecha de nacimiento es requerida.")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate birthDate;

    @NotBlank(message = "La contraseña es requerida.")
    private String password;

    @NotBlank(message = "Debes confirmar la contraseña.")
    private String confirmPassword;
}
