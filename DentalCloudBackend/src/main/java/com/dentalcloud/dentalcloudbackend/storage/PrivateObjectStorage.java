package com.dentalcloud.dentalcloudbackend.storage;

import java.io.IOException;
import java.io.InputStream;

/** Private object storage boundary; production can replace the filesystem adapter with S3/MinIO. */
public interface PrivateObjectStorage {
    void put(String objectKey, InputStream content, long contentLength) throws IOException;

    StoredObject get(String objectKey) throws IOException;

    record StoredObject(InputStream content, long contentLength) {
    }
}
