package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelarCitaRequestDTO {
    @Size(max = 500, message = "El motivo de cancelación no puede exceder los 500 caracteres")
    private String motivoCancelacion;
}
