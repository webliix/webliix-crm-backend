package com.webliix.blog.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Slf4j
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.url:}")
    private String cloudinaryUrl;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        // Option 1: Direct CLOUDINARY_URL (e.g. cloudinary://key:secret@cloud_name)
        String envUrl = System.getenv("CLOUDINARY_URL");
        if (envUrl == null || envUrl.isBlank()) {
            envUrl = System.getProperty("CLOUDINARY_URL");
        }
        if (envUrl == null || envUrl.isBlank()) {
            envUrl = cloudinaryUrl;
        }

        if (envUrl != null && !envUrl.isBlank()) {
            log.info("Cloudinary initialized via CLOUDINARY_URL");
            return new Cloudinary(envUrl.trim());
        }

        // Option 2: Individual credentials
        String cleanCloudName = cloudName != null ? cloudName.trim().toLowerCase() : "";
        String cleanApiKey = apiKey != null ? apiKey.trim() : "";
        String cleanApiSecret = apiSecret != null ? apiSecret.trim() : "";

        if (cleanCloudName.isBlank() || cleanApiKey.isBlank() || cleanApiSecret.isBlank()) {
            log.warn("Cloudinary configuration is missing or incomplete! Please check CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET in your .env");
        } else {
            log.info("Cloudinary initialized successfully for cloud: '{}'", cleanCloudName);
        }

        Map<String, Object> config = ObjectUtils.asMap(
                "cloud_name", cleanCloudName,
                "api_key", cleanApiKey,
                "api_secret", cleanApiSecret,
                "secure", true
        );

        return new Cloudinary(config);
    }
}
