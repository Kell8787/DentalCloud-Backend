package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalDocumentUploadRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.ClinicalDocument;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.PatientTreatmentPlan;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.repositories.CitasRepository;
import com.dentalcloud.dentalcloudbackend.repositories.ClinicalDocumentRepository;
import com.dentalcloud.dentalcloudbackend.repositories.DentistRepository;
import com.dentalcloud.dentalcloudbackend.repositories.PatientTreatmentPlanRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import com.dentalcloud.dentalcloudbackend.storage.DocumentDownloadSigner;
import com.dentalcloud.dentalcloudbackend.storage.PrivateObjectStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicalDocumentServiceTest {
    @Mock private ClinicalDocumentRepository documentRepository;
    @Mock private UserRepository userRepository;
    @Mock private CitasRepository citasRepository;
    @Mock private PatientTreatmentPlanRepository planRepository;
    @Mock private DentistRepository dentistRepository;
    @Mock private PrivateObjectStorage storage;
    @Mock private DocumentDownloadSigner signer;
    @InjectMocks private ClinicalDocumentService service;

    @Test
    void calculatesChecksumAndNeverReturnsPrivateObjectKey() throws Exception {
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        User doctor = User.builder().id(doctorId).email("doctor@example.com").role(Rol.DOCTOR).build();
        Dentist dentist = Dentist.builder().id(UUID.randomUUID()).user(doctor).build();
        PatientTreatmentPlan plan = PatientTreatmentPlan.builder().id(planId).patientId(patientId)
                .dentistId(dentist.getId()).build();
        ClinicalDocumentUploadRequestDTO request = new ClinicalDocumentUploadRequestDTO();
        request.setPatientId(patientId);
        request.setPlanId(planId);
        request.setDocumentType("XRAY");
        request.setTitle("Radiografía");
        MockMultipartFile file = new MockMultipartFile("file", "xray.pdf", "application/pdf",
                "%PDF-1.7 test".getBytes());
        UUID documentId = UUID.randomUUID();

        when(userRepository.findByEmail(doctor.getEmail())).thenReturn(Optional.of(doctor));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(dentistRepository.findByUser(doctor)).thenReturn(Optional.of(dentist));
        when(signer.sign(documentId)).thenReturn(new DocumentDownloadSigner.SignedDownload(123L, "signed"));
        when(documentRepository.save(any(ClinicalDocument.class))).thenAnswer(invocation -> {
            ClinicalDocument document = invocation.getArgument(0);
            document.setId(documentId);
            return document;
        });

        var response = service.upload(request, file, doctor.getEmail());

        assertThat(response.getChecksum()).hasSize(64);
        assertThat(response.getDownloadUrl()).contains("expiresAt=123").contains("signature=signed");
        assertThat(response.getDownloadUrl()).doesNotContain("objectKey");
    }
}
