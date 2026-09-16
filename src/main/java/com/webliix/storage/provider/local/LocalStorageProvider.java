package com.webliix.storage.provider.local;

import com.webliix.storage.provider.StorageProvider;
import com.webliix.storage.util.StoragePathUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class LocalStorageProvider implements StorageProvider {

    @Value("${storage.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public String upload(MultipartFile file) throws IOException {
        Path targetFolder = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(targetFolder);
        String storagePath = StoragePathUtil.buildStoragePath(file.getOriginalFilename());
        Path targetPath = targetFolder.resolve(storagePath);
        Files.createDirectories(targetPath.getParent());
        file.transferTo(targetPath.toFile());
        return targetPath.toString();
    }

    @Override
    public Resource download(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("File not found: " + path);
        }
        return new FileSystemResource(file);
    }

    @Override
    public void delete(String path) {
        File file = new File(path);
        if (file.exists()) {
            FileSystemUtils.deleteRecursively(file);
        }
    }

    @Override
    public boolean supports(String providerName) {
        return "local".equalsIgnoreCase(providerName);
    }
}
