package com.dentalcloud.dentalcloudbackend.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class InventoryUpdateRequestDTO {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal purchasePrice;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal salePrice;

    @NotNull
    private UUID categoryId;

    @NotNull
    @Min(0)
    private Integer quantity;

    @Min(0)
    private Integer minimumStock;

    @Size(max = 32)
    private String unit;
}
