package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PatientDashboardResponseDTO {
    private List<PatientTreatmentPlanResponseDTO> plans;
    private CitaResponseDTO nextAppointment;
    private List<CitaResponseDTO> appointmentHistory;
    private List<AftercareInstructionResponseDTO> aftercareInstructions;
    private List<ClinicalDocumentResponseDTO> documents;
}
