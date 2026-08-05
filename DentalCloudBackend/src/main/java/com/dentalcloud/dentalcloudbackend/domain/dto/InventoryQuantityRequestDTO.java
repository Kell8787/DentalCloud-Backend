package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InventoryQuantityRequestDTO {
    @NotNull
    @Min(1)
    private Integer quantity;

    @NotBlank
    @Size(max = 500)
    private String reason;
}
