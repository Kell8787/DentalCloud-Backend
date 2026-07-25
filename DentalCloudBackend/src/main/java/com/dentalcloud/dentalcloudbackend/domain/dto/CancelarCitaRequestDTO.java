package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.dentalcloud.dentalcloudbackend.domain.enums.MotivoCancelacion;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancelarCitaRequestDTO {
    @NotNull(message = "El motivo de cancelación es obligatorio")
    private MotivoCancelacion motivoCancelacion;
}
