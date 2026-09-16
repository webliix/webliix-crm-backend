package com.webliix.storage.controller;

import com.webliix.storage.dto.StoredFileResponse;
import com.webliix.storage.dto.UploadFileResponse;
import com.webliix.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<UploadFileResponse> upload(@RequestPart("file") MultipartFile file,
                                                     @RequestParam(value = "tenantId", required = false) Long tenantId,
                                                     @RequestParam(value = "module", required = false) String module,
                                                     @RequestParam(value = "referenceId", required = false) Long referenceId) throws Exception {
        return ResponseEntity.ok(storageService.uploadFile(file, tenantId, module, referenceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable("id") Long id) throws Exception {
        Resource resource = storageService.downloadFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) throws Exception {
        storageService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/metadata/{id}")
    public ResponseEntity<StoredFileResponse> metadata(@PathVariable("id") Long id) {
        return ResponseEntity.ok(storageService.getMetadata(id));
    }
}
