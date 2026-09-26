package com.dentalcloud.dentalcloudbackend.domain.entity;

import com.dentalcloud.dentalcloudbackend.domain.enums.Genero;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "DentalUsers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String firstName;

    private String secondName;

    @NotBlank
    private String lastName;

    private String secondLastName;

    @NotBlank
    @Column(nullable = false)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genero genero;

    @Column(unique = true, nullable = false, length = 10)
    private String dui;

    @NotNull
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate birthDate;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Rol role;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
