package com.webliix.mobile.storage;

import org.springframework.stereotype.Service;

@Service
public class FileStorageService {

    // Generate a signed URL for file downloads (MinIO/S3 compatible).
    public String generateDownloadUrl(String objectKey, long expirySeconds) {
        // TODO: implement using MinIO client or S3 SDK
        return "";
    }
}
