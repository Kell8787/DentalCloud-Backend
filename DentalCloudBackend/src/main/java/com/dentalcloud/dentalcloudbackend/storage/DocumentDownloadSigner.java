package com.dentalcloud.dentalcloudbackend.storage;

import com.dentalcloud.dentalcloudbackend.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Component
public class DocumentDownloadSigner {
    private final byte[] secret;
    private final long ttlSeconds;

    public DocumentDownloadSigner(
            @Value("${dentalcloud.documents.signing-secret:${jwt.secret}}") String secret,
            @Value("${dentalcloud.documents.download-ttl-seconds:300}") long ttlSeconds) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("dentalcloud.documents.signing-secret es obligatorio");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = ttlSeconds;
    }

    public SignedDownload sign(UUID documentId) {
        long expiresAt = Instant.now().plusSeconds(ttlSeconds).getEpochSecond();
        return new SignedDownload(expiresAt, signature(documentId, expiresAt));
    }

    public void verify(UUID documentId, long expiresAt, String signature) {
        if (expiresAt < Instant.now().getEpochSecond()
                || !constantTimeEquals(signature(documentId, expiresAt), signature)) {
            throw new BusinessException("La URL de descarga no es válida o ya expiró");
        }
    }

    private String signature(UUID documentId, long expiresAt) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] value = mac.doFinal((documentId + "." + expiresAt).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo firmar la URL de descarga", exception);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (actual == null) {
            return false;
        }
        return java.security.MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                actual.getBytes(StandardCharsets.US_ASCII));
    }

    public record SignedDownload(long expiresAt, String signature) {
    }
}
