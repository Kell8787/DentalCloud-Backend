package com.dentalcloud.dentalcloudbackend.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ClinicalRecordResponseDTO {
    private UUID patientId;
    private UserResponseDTO patient;
    private InformacionMedicaDTO medicalSummary;
    private List<CitaResponseDTO> appointments;
    private List<PatientTreatmentPlanResponseDTO> plans;
    private List<ClinicalNoteResponseDTO> notes;
    private List<AftercareInstructionResponseDTO> aftercareInstructions;
    private List<ClinicalDocumentResponseDTO> documents;
}
