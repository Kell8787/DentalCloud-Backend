package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalRecordResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.PatientTreatmentPlanResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.exceptions.BusinessException;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClinicalRecordService {
    private final UserRepository userRepository;
    private final PatientService patientService;
    private final CitaService citaService;
    private final TreatmentPlanService treatmentPlanService;
    private final AftercareInstructionService aftercareInstructionService;
    private final ClinicalDocumentService clinicalDocumentService;

    public ClinicalRecordResponseDTO get(UUID patientId, String actorEmail) {
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (actor.getRole() != Rol.DOCTOR && actor.getRole() != Rol.ADMIN) {
            throw new BusinessException("Solo el doctor o administrador puede consultar expedientes clínicos");
        }
        User patient = userRepository.findById(patientId)
                .filter(user -> user.getRole() == Rol.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
        List<com.dentalcloud.dentalcloudbackend.domain.dto.CitaResponseDTO> appointments =
                citaService.obtenerCitasDeExpediente(patient.getId(), actorEmail);
        List<PatientTreatmentPlanResponseDTO> plans = treatmentPlanService.listForPatient(patient.getId(), actorEmail);
        if (actor.getRole() == Rol.DOCTOR && appointments.isEmpty() && plans.isEmpty()) {
            throw new ResourceNotFoundException("Expediente clínico no encontrado");
        }
        return ClinicalRecordResponseDTO.builder()
                .patientId(patient.getId())
                .patient(patientService.obtenerPerfil(patient.getId()))
                .medicalSummary(patientService.obtenerInformacionMedica(patient.getId()))
                .appointments(appointments)
                .plans(plans)
                .aftercareInstructions(aftercareInstructionService.listForClinicalRecord(patient.getId(), actorEmail))
                .documents(clinicalDocumentService.listForClinicalRecord(patient.getId(), actorEmail))
                .build();
    }
}
