package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TratamientoEstadoRequestDTO {
    @NotNull
    private Boolean active;
}
