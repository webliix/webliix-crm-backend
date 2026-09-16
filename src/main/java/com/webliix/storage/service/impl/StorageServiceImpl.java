package com.webliix.storage.service.impl;

import com.webliix.shared.exceptions.ResourceNotFoundException;
import com.webliix.storage.dto.StoredFileResponse;
import com.webliix.storage.dto.UploadFileResponse;
import com.webliix.storage.entity.StoredFile;
import com.webliix.storage.provider.StorageProvider;
import com.webliix.storage.repository.StoredFileRepository;
import com.webliix.storage.service.StorageService;
import com.webliix.storage.util.StoragePathUtil;
import com.webliix.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final StoredFileRepository storedFileRepository;
    private final UserRepository userRepository;
    private final List<StorageProvider> storageProviders;

    @Value("${storage.provider:local}")
    private String storageProviderName;

    @Value("${storage.base-url:/api/v1/storage}")
    private String storageBaseUrl;

    @Override
    @Transactional
    public UploadFileResponse uploadFile(MultipartFile file, Long tenantId, String module, Long referenceId) throws Exception {
        StorageProvider provider = getActiveProvider();
        String storagePath = provider.upload(file);
        Long uploadedBy = resolveCurrentUserId();
        Long effectiveTenantId = tenantId != null ? tenantId : 1L;

        StoredFile storedFile = StoredFile.builder()
                .fileName(StoragePathUtil.toFileName(storagePath))
                .originalName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .storageProvider(provider.getClass().getSimpleName())
                .storagePath(storagePath)
                .tenantId(effectiveTenantId)
                .uploadedBy(uploadedBy)
                .module(module)
                .referenceId(referenceId)
                .createdAt(LocalDateTime.now())
                .build();

        StoredFile saved = storedFileRepository.save(storedFile);
        String url = storageBaseUrl + "/" + saved.getId();
        return UploadFileResponse.builder()
                .fileId(saved.getId())
                .url(url)
                .build();
    }

    @Override
    public Resource downloadFile(Long id) throws Exception {
        StoredFile storedFile = storedFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StoredFile not found with id: " + id));
        StorageProvider provider = getActiveProvider();
        return provider.download(storedFile.getStoragePath());
    }

    @Override
    @Transactional
    public void deleteFile(Long id) throws Exception {
        StoredFile storedFile = storedFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StoredFile not found with id: " + id));
        StorageProvider provider = getActiveProvider();
        provider.delete(storedFile.getStoragePath());
        storedFileRepository.delete(storedFile);
    }

    @Override
    public StoredFileResponse getMetadata(Long id) {
        StoredFile storedFile = storedFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StoredFile not found with id: " + id));
        return StoredFileResponse.builder()
                .id(storedFile.getId())
                .fileName(storedFile.getFileName())
                .originalName(storedFile.getOriginalName())
                .fileSize(storedFile.getFileSize())
                .contentType(storedFile.getContentType())
                .storageProvider(storedFile.getStorageProvider())
                .storagePath(storedFile.getStoragePath())
                .tenantId(storedFile.getTenantId())
                .uploadedBy(storedFile.getUploadedBy())
                .module(storedFile.getModule())
                .referenceId(storedFile.getReferenceId())
                .createdAt(storedFile.getCreatedAt())
                .build();
    }

    private StorageProvider getActiveProvider() {
        return storageProviders.stream()
                .filter(provider -> provider.supports(storageProviderName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No storage provider configured for: " + storageProviderName));
    }

    private Long resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .map(user -> user.getId())
                    .orElse(null);
        }
        return null;
    }
}
