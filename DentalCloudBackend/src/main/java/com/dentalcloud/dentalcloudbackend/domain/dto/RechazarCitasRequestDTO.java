package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RechazarCitasRequestDTO {
    @Size(max = 255, message = "El motivo no puede exceder 255 caracteres")
    private String motivo;
}
