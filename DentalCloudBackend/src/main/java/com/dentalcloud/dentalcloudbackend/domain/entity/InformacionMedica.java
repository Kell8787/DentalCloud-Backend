package com.dentalcloud.dentalcloudbackend.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "InformacionMedica")
public class InformacionMedica {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ElementCollection
    @CollectionTable(name = "user_alergias", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "alergia")
    private List<String> alergias;

    @ElementCollection
    @CollectionTable(name = "user_medicamentos", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "medicamentos")
    private List<String> medicamentos;

    @Column(columnDefinition = "TEXT")
    private String antecedentesMedicos;
}
