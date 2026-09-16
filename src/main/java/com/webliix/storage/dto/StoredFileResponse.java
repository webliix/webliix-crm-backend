package com.webliix.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredFileResponse {
    private Long id;
    private String fileName;
    private String originalName;
    private Long fileSize;
    private String contentType;
    private String storageProvider;
    private String storagePath;
    private Long tenantId;
    private Long uploadedBy;
    private String module;
    private Long referenceId;
    private LocalDateTime createdAt;
}
