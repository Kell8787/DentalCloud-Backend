package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.dentalcloud.dentalcloudbackend.domain.enums.Parentesco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContactoEmergenciaDTO {
    private String nombreCompleto;
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9]{4}-[0-9]{4}$", message = "El teléfono debe tener el formato xxxx-xxxx")
    private String phoneNumber;

    private Parentesco parentesco;
}
