package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class InventoryReconciliationResponseDTO {
    private UUID productId;
    private Integer currentQuantity;
    private Integer movementBalance;
    private Integer discrepancy;
    private long entries;
    private long exits;
    private long adjustments;
    private boolean consistent;
}
