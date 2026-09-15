package com.webliix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebliixApplication {

	public static void main(String[] args) {
		loadDotenv();
		SpringApplication.run(WebliixApplication.class, args);
	}

	private static void loadDotenv() {
		try {
			// Check current working directory, user.dir, or parent directory for .env
			Path[] candidates = new Path[]{
					Path.of(".env"),
					Path.of(System.getProperty("user.dir"), ".env"),
					Path.of("Backend/webliix/.env"),
					Path.of("../.env")
			};

			for (Path envPath : candidates) {
				if (Files.exists(envPath)) {
					List<String> lines = Files.readAllLines(envPath);
					for (String line : lines) {
						line = line.trim();
						if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
							continue;
						}
						int eqIdx = line.indexOf('=');
						String key = line.substring(0, eqIdx).trim();
						String value = line.substring(eqIdx + 1).trim();

						// Strip surrounding quotes
						if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
							value = value.substring(1, value.length() - 1);
						}

						if (System.getProperty(key) == null && System.getenv(key) == null) {
							System.setProperty(key, value);
						}
					}
					break;
				}
			}
		} catch (Exception ignored) {
		}
	}
}
