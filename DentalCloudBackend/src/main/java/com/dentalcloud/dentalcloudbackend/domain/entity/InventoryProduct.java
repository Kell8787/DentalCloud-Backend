package com.dentalcloud.dentalcloudbackend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "InventoryProducts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal purchasePrice;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal salePrice;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Min(0)
    @Builder.Default
    @Column(name = "minimum_stock", nullable = false)
    private Integer minimumStock = 0;

    @NotBlank
    @Builder.Default
    @Column(nullable = false, length = 32)
    private String unit = "unidad";

    @Version
    @Builder.Default
    @Column(nullable = false)
    private Long version = 0L;

    @Builder.Default
    private boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;
}
