package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.dentalcloud.dentalcloudbackend.domain.enums.Parentesco;
import lombok.Data;

@Data
public class ContactoEmergenciaDTO {
    private String nombreCompleto;
    private String email;
    private String phoneNumber;
    private Parentesco parentesco;
}
