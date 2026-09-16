package com.webliix.storage.provider.s3;

import com.webliix.storage.provider.StorageProvider;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class S3StorageProvider implements StorageProvider {

    @Override
    public String upload(MultipartFile file) {
        throw new UnsupportedOperationException("S3 storage provider is not implemented yet");
    }

    @Override
    public Resource download(String path) {
        throw new UnsupportedOperationException("S3 storage provider is not implemented yet");
    }

    @Override
    public void delete(String path) {
        throw new UnsupportedOperationException("S3 storage provider is not implemented yet");
    }

    @Override
    public boolean supports(String providerName) {
        return "s3".equalsIgnoreCase(providerName);
    }
}
