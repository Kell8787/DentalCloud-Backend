package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.TreatmentStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TreatmentStepRepository extends JpaRepository<TreatmentStep, UUID> {

    List<TreatmentStep> findByPlanIdOrderByPositionAsc(UUID planId);

    boolean existsByPlanIdAndPosition(UUID planId, Integer position);
}
