package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AppointmentPageResponseDTO {
    private List<CitaResponseDTO> items;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
}
