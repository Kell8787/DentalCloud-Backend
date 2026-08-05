package com.dentalcloud.dentalcloudbackend.storage;

import com.dentalcloud.dentalcloudbackend.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class LocalPrivateObjectStorage implements PrivateObjectStorage {
    private final Path root;

    public LocalPrivateObjectStorage(
            @Value("${dentalcloud.storage.local-root:${java.io.tmpdir}/dentalcloud-private}") String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    @Override
    public void put(String objectKey, InputStream content, long contentLength) throws IOException {
        Path target = safePath(objectKey);
        Files.createDirectories(target.getParent());
        try (InputStream input = content) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public StoredObject get(String objectKey) throws IOException {
        Path target = safePath(objectKey);
        if (!Files.isRegularFile(target)) {
            throw new IOException("Objeto privado no encontrado");
        }
        return new StoredObject(Files.newInputStream(target), Files.size(target));
    }

    private Path safePath(String objectKey) {
        Path target = root.resolve(objectKey).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException("La clave del objeto no es válida");
        }
        return target;
    }
}
