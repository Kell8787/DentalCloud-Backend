package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryQuantityRequestDTO {
    @NotNull
    @Min(1)
    private Integer quantity;
}

