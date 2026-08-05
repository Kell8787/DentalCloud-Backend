package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalDocumentResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalDocumentUploadRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.Citas;
import com.dentalcloud.dentalcloudbackend.domain.entity.ClinicalDocument;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.PatientTreatmentPlan;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.exceptions.BusinessException;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.repositories.CitasRepository;
import com.dentalcloud.dentalcloudbackend.repositories.ClinicalDocumentRepository;
import com.dentalcloud.dentalcloudbackend.repositories.DentistRepository;
import com.dentalcloud.dentalcloudbackend.repositories.PatientTreatmentPlanRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import com.dentalcloud.dentalcloudbackend.storage.DocumentDownloadSigner;
import com.dentalcloud.dentalcloudbackend.storage.PrivateObjectStorage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClinicalDocumentService {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("application/pdf", "image/jpeg", "image/png");

    private final ClinicalDocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CitasRepository citasRepository;
    private final PatientTreatmentPlanRepository planRepository;
    private final DentistRepository dentistRepository;
    private final PrivateObjectStorage storage;
    private final DocumentDownloadSigner signer;

    @Transactional
    public ClinicalDocumentResponseDTO upload(ClinicalDocumentUploadRequestDTO request,
                                              MultipartFile file,
                                              String actorEmail) {
        User actor = user(actorEmail);
        if (actor.getRole() != Rol.DOCTOR && actor.getRole() != Rol.ADMIN) {
            throw new BusinessException("Solo el doctor o administrador puede gestionar documentos clínicos");
        }
        if (file == null || file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("El archivo es obligatorio y no puede superar 10 MB");
        }
        String mimeType = normalizeMime(file.getContentType());
        validateFileType(file, mimeType);
        UUID patientId = resolveParentAndPatient(request, actor);
        String objectKey = "clinical/" + patientId + "/" + UUID.randomUUID();
        String checksum;
        try {
            checksum = checksum(file.getInputStream());
            storage.put(objectKey, file.getInputStream(), file.getSize());
        } catch (IOException exception) {
            throw new BusinessException("No se pudo almacenar el documento");
        }

        ClinicalDocument document = ClinicalDocument.builder()
                .patientId(patientId)
                .appointmentId(request.getAppointmentId())
                .planId(request.getPlanId())
                .documentType(request.getDocumentType().trim())
                .title(request.getTitle().trim())
                .objectKey(objectKey)
                .mimeType(mimeType)
                .sizeBytes(file.getSize())
                .checksum(checksum)
                .visibleToPatient(request.isVisibleToPatient())
                .createdBy(actor.getId())
                .createdAt(Instant.now())
                .build();
        return map(documentRepository.save(document));
    }

    @Transactional
    public List<ClinicalDocumentResponseDTO> listOwn(String email) {
        User patient = user(email);
        return documentRepository.findByPatientIdAndVisibleToPatientTrueOrderByCreatedAtDesc(patient.getId())
                .stream().map(this::map).toList();
    }

    @Transactional
    public ClinicalDocumentResponseDTO get(UUID id, String email) {
        ClinicalDocument document = document(id);
        assertCanAccess(document, user(email));
        return map(document);
    }

    @Transactional
    public DownloadedDocument download(UUID id, long expiresAt, String signature) {
        signer.verify(id, expiresAt, signature);
        ClinicalDocument document = document(id);
        try {
            PrivateObjectStorage.StoredObject stored = storage.get(document.getObjectKey());
            return new DownloadedDocument(new InputStreamResource(stored.content()), document.getMimeType(),
                    stored.contentLength(), document.getTitle());
        } catch (IOException exception) {
            throw new ResourceNotFoundException("Contenido del documento no encontrado");
        }
    }

    private UUID resolveParentAndPatient(ClinicalDocumentUploadRequestDTO request, User actor) {
        if (request.getAppointmentId() == null && request.getPlanId() == null) {
            throw new BusinessException("El documento debe relacionarse con una cita o un plan");
        }
        UUID patientId = request.getPatientId();
        Citas appointment = null;
        PatientTreatmentPlan plan = null;
        if (request.getAppointmentId() != null) {
            appointment = citasRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
            patientId = requireSamePatient(patientId, appointment.getUser().getId());
            if (request.getPlanId() != null && !request.getPlanId().equals(appointment.getTreatmentPlanId())) {
                throw new BusinessException("La cita y el plan no coinciden");
            }
            assertDoctorOwnsAppointment(actor, appointment);
        }
        if (request.getPlanId() != null) {
            plan = planRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plan de tratamiento no encontrado"));
            patientId = requireSamePatient(patientId, plan.getPatientId());
            assertDoctorOwnsPlan(actor, plan);
        }
        if (patientId == null) {
            throw new BusinessException("El paciente es requerido para el documento");
        }
        if (actor.getRole() == Rol.DOCTOR && dentistRepository.findByUser(actor).isEmpty()) {
            throw new BusinessException("El usuario no tiene un perfil de doctor válido");
        }
        return patientId;
    }

    private UUID requireSamePatient(UUID requestedPatientId, UUID parentPatientId) {
        if (requestedPatientId != null && !requestedPatientId.equals(parentPatientId)) {
            throw new BusinessException("El paciente no coincide con el recurso clínico");
        }
        return parentPatientId;
    }

    private void assertDoctorOwnsAppointment(User actor, Citas appointment) {
        if (actor.getRole() == Rol.DOCTOR && (appointment.getDentist() == null
                || appointment.getDentist().getUser() == null
                || !appointment.getDentist().getUser().getId().equals(actor.getId()))) {
            throw new BusinessException("No puedes gestionar documentos de otro doctor");
        }
    }

    private void assertDoctorOwnsPlan(User actor, PatientTreatmentPlan plan) {
        if (actor.getRole() == Rol.DOCTOR && dentistRepository.findByUser(actor)
                .map(Dentist::getId).filter(plan.getDentistId()::equals).isEmpty()) {
            throw new BusinessException("No puedes gestionar documentos de otro doctor");
        }
    }

    private void assertCanAccess(ClinicalDocument document, User actor) {
        if (actor.getRole() == Rol.ADMIN) {
            return;
        }
        if (actor.getRole() == Rol.CUSTOMER && document.getPatientId().equals(actor.getId())
                && document.isVisibleToPatient()) {
            return;
        }
        if (actor.getRole() == Rol.DOCTOR) {
            if (document.getPlanId() != null) {
                PatientTreatmentPlan plan = planRepository.findById(document.getPlanId()).orElse(null);
                if (plan != null && dentistRepository.findByUser(actor).map(Dentist::getId)
                        .filter(plan.getDentistId()::equals).isPresent()) {
                    return;
                }
            }
            if (document.getAppointmentId() != null) {
                Citas appointment = citasRepository.findById(document.getAppointmentId()).orElse(null);
                if (appointment != null && appointment.getDentist() != null
                        && appointment.getDentist().getUser() != null
                        && appointment.getDentist().getUser().getId().equals(actor.getId())) {
                    return;
                }
            }
        }
        throw new ResourceNotFoundException("Documento no encontrado");
    }

    private void validateFileType(MultipartFile file, String mimeType) {
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new BusinessException("El tipo de archivo no está permitido");
        }
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(8);
            boolean valid = switch (mimeType) {
                case "application/pdf" -> startsWith(header, new byte[]{0x25, 0x50, 0x44, 0x46});
                case "image/jpeg" -> startsWith(header, new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff});
                case "image/png" -> startsWith(header, new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a});
                default -> false;
            };
            if (!valid) {
                throw new BusinessException("El contenido no coincide con el MIME declarado");
            }
        } catch (IOException exception) {
            throw new BusinessException("No se pudo validar el archivo");
        }
    }

    private boolean startsWith(byte[] actual, byte[] expected) {
        if (actual.length < expected.length) {
            return false;
        }
        for (int index = 0; index < expected.length; index++) {
            if (actual[index] != expected[index]) {
                return false;
            }
        }
        return true;
    }

    private String normalizeMime(String mimeType) {
        return mimeType == null ? "" : mimeType.split(";", 2)[0].trim().toLowerCase();
    }

    private String checksum(InputStream input) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(input.readAllBytes());
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no disponible", exception);
        }
    }

    private User user(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private ClinicalDocument document(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado"));
    }

    private ClinicalDocumentResponseDTO map(ClinicalDocument document) {
        DocumentDownloadSigner.SignedDownload signed = signer.sign(document.getId());
        return ClinicalDocumentResponseDTO.builder()
                .id(document.getId()).patientId(document.getPatientId()).appointmentId(document.getAppointmentId())
                .planId(document.getPlanId()).documentType(document.getDocumentType()).title(document.getTitle())
                .mimeType(document.getMimeType()).sizeBytes(document.getSizeBytes()).checksum(document.getChecksum())
                .visibleToPatient(document.isVisibleToPatient()).createdBy(document.getCreatedBy())
                .createdAt(document.getCreatedAt())
                .downloadUrl("/api/documentos/" + document.getId() + "/contenido?expiresAt="
                        + signed.expiresAt() + "&signature=" + signed.signature())
                .build();
    }

    public record DownloadedDocument(Resource resource, String mimeType, long contentLength, String filename) {
    }
}
