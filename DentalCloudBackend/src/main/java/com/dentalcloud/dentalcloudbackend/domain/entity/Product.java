package com.dentalcloud.dentalcloudbackend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "DentalProducts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String name;

    private Integer cantidad;

    @NotBlank
    private String marca;

    @NotBlank
    private String vendedor;

    @Column(unique = true, nullable = false, length = 20)
    private String vendedorPhoneNumber;

    private LocalDate reStockDate;
}
