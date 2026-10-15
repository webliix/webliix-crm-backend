package com.webliix.storage.controller;

import com.webliix.security.annotation.RequirePermission;
import com.webliix.storage.dto.StoredFileResponse;
import com.webliix.storage.dto.UploadFileResponse;
import com.webliix.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/upload")
    @RequirePermission(anyOf = {"STORAGE_UPLOAD", "STORAGE_MANAGE"})
    public ResponseEntity<UploadFileResponse> upload(@RequestPart("file") MultipartFile file,
                                                     @RequestParam(value = "tenantId", required = false) Long tenantId,
                                                     @RequestParam(value = "module", required = false) String module,
                                                     @RequestParam(value = "referenceId", required = false) Long referenceId) throws Exception {
        return ResponseEntity.ok(storageService.uploadFile(file, tenantId, module, referenceId));
    }

    @GetMapping("/{id}")
    @RequirePermission(anyOf = {"STORAGE_UPLOAD", "STORAGE_MANAGE", "EMPLOYEE_PORTAL_ACCESS", "CLIENT_PORTAL_ACCESS", "DASHBOARD_VIEW"})
    public ResponseEntity<Resource> download(@PathVariable("id") Long id) throws Exception {
        Resource resource = storageService.downloadFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @RequirePermission("STORAGE_MANAGE")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) throws Exception {
        storageService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/metadata/{id}")
    @RequirePermission(anyOf = {"STORAGE_UPLOAD", "STORAGE_MANAGE"})
    public ResponseEntity<StoredFileResponse> metadata(@PathVariable("id") Long id) {
        return ResponseEntity.ok(storageService.getMetadata(id));
    }

    @GetMapping("/files")
    @RequirePermission(anyOf = {"STORAGE_UPLOAD", "STORAGE_MANAGE"})
    public ResponseEntity<List<StoredFileResponse>> listFiles(
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "referenceId", required = false) Long referenceId) {
        return ResponseEntity.ok(storageService.getFiles(module, referenceId));
    }

    @GetMapping("/module/{module}/{referenceId}")
    @RequirePermission(anyOf = {"STORAGE_UPLOAD", "STORAGE_MANAGE"})
    public ResponseEntity<List<StoredFileResponse>> listFilesByModule(
            @PathVariable("module") String module,
            @PathVariable("referenceId") Long referenceId) {
        return ResponseEntity.ok(storageService.getFiles(module, referenceId));
    }
}
