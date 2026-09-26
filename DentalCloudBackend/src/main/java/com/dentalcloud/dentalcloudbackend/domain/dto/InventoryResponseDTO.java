package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class InventoryResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private UUID categoryId;
    private String categoryName;
    private Integer quantity;
    private Integer minimumStock;
    private String unit;
    private Long version;
    private String status;
}
