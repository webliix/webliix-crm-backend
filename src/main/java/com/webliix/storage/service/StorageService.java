package com.webliix.storage.service;

import com.webliix.storage.dto.StoredFileResponse;
import com.webliix.storage.dto.UploadFileResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    UploadFileResponse uploadFile(MultipartFile file, Long tenantId, String module, Long referenceId) throws Exception;

    Resource downloadFile(Long id) throws Exception;

    void deleteFile(Long id) throws Exception;

    StoredFileResponse getMetadata(Long id);
}
