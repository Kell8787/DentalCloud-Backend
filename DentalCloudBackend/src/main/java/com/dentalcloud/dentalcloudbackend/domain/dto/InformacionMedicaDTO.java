package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class InformacionMedicaDTO {
    private List<String> alergias;
    private List<String> medicamentos;
    private String antecedentesMedicos;
}
