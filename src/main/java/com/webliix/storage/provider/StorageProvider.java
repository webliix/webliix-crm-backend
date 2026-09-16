package com.webliix.storage.provider;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageProvider {

    String upload(MultipartFile file) throws Exception;

    Resource download(String path) throws Exception;

    void delete(String path) throws Exception;

    boolean supports(String providerName);
}
