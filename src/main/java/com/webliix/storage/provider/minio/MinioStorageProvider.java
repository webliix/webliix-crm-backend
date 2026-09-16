package com.webliix.storage.provider.minio;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.webliix.storage.provider.StorageProvider;
import com.webliix.storage.util.StoragePathUtil;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MinioStorageProvider implements StorageProvider {

    @Value("${minio.endpoint:}")
    private String endpoint;

    @Value("${minio.access-key:}")
    private String accessKey;

    @Value("${minio.secret-key:}")
    private String secretKey;

    @Value("${minio.bucket:webliix}")
    private String bucket;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Override
    public String upload(MultipartFile file) throws Exception {
        String storagePath = StoragePathUtil.buildStoragePath(file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(storagePath)
                    .stream(inputStream, file.getSize(), -1L)
                    .contentType(file.getContentType())
                    .build();
            ObjectWriteResponse response = minioClient.putObject(args);
            return storagePath;
        }
    }

    @Override
    public Resource download(String path) throws Exception {
        InputStream objectStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(path)
                .build());
        return new InputStreamResource(objectStream);
    }

    @Override
    public void delete(String path) throws Exception {
        minioClient.removeObject(io.minio.RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(path)
                .build());
    }

    @Override
    public boolean supports(String providerName) {
        return "minio".equalsIgnoreCase(providerName);
    }
}
