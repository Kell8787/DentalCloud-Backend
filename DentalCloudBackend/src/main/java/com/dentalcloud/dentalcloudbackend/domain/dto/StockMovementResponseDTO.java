package com.dentalcloud.dentalcloudbackend.domain.dto;

import com.dentalcloud.dentalcloudbackend.domain.enums.StockMovementType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class StockMovementResponseDTO {
    private UUID id;
    private UUID productId;
    private StockMovementType type;
    private Integer quantity;
    private String reason;
    private UUID actorId;
    private Instant occurredAt;
}
