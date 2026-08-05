package com.dentalcloud.dentalcloudbackend.storage;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentDownloadSignerTest {
    @Test
    void rejectsSignatureForAnotherDocument() {
        DocumentDownloadSigner signer = new DocumentDownloadSigner("test-secret", 300);
        UUID documentId = UUID.randomUUID();
        var signed = signer.sign(documentId);

        assertThatThrownBy(() -> signer.verify(UUID.randomUUID(), signed.expiresAt(), signed.signature()))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.BusinessException.class);
    }
}
