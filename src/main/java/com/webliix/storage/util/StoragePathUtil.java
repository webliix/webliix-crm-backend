package com.webliix.storage.util;

import java.nio.file.Paths;
import java.util.UUID;

public class StoragePathUtil {

    public static String buildStoragePath(String originalFilename) {
        String safeName = originalFilename == null ? "file" : originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String unique = UUID.randomUUID().toString();
        return Paths.get(unique + "_" + safeName).toString().replace("\\", "/");
    }

    public static String toFileName(String storagePath) {
        if (storagePath == null) {
            return null;
        }
        int lastSlash = storagePath.lastIndexOf('/');
        return lastSlash >= 0 ? storagePath.substring(lastSlash + 1) : storagePath;
    }
}
