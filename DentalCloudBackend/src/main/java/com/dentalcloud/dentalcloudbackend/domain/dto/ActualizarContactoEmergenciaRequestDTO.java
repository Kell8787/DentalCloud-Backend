package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarContactoEmergenciaRequestDTO {
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombreCompleto;

    @Email(message = "El email no es válido")
    private String email;

    @Pattern(regexp = "^[0-9]{4}-[0-9]{4}$", message = "El teléfono debe tener el formato xxxx-xxxx")
    private String phoneNumber;

    @Pattern(regexp = "MADRE|PADRE|HERMANO|HERMANA|PAREJA|TUTOR|OTRO", message = "Parentesco no válido")
    private String parentesco;
}
