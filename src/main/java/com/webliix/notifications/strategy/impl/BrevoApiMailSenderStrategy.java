package com.webliix.notifications.strategy.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webliix.notifications.strategy.MailSenderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BrevoApiMailSenderStrategy implements MailSenderStrategy {

    private final String apiKey;
    private final String apiUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BrevoApiMailSenderStrategy(
            @Value("${brevo.api.key:${BREVO_API_KEY:${MAIL_PASSWORD:}}}") String apiKey,
            @Value("${brevo.api.url:https://api.brevo.com/v3/smtp/email}") String apiUrl,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.apiUrl = apiUrl != null && !apiUrl.isBlank() ? apiUrl.trim() : "https://api.brevo.com/v3/smtp/email";
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public boolean sendMail(String fromEmail, String fromName, String toEmail, String subject, String content, boolean isHtml) {
        if (!isAvailable()) {
            log.warn("Brevo API strategy invoked but API key is missing or unconfigured.");
            return false;
        }

        try {
            Map<String, Object> payload = new HashMap<>();

            Map<String, String> sender = new HashMap<>();
            sender.put("email", fromEmail);
            sender.put("name", fromName != null && !fromName.isBlank() ? fromName : "Webliix");
            payload.put("sender", sender);

            Map<String, String> recipient = new HashMap<>();
            recipient.put("email", toEmail);
            payload.put("to", List.of(recipient));

            payload.put("subject", subject);

            if (isHtml) {
                payload.put("htmlContent", content);
            } else {
                payload.put("textContent", content);
            }

            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .header("api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Successfully sent email via Brevo REST API to: {} [Status: {}]", toEmail, response.statusCode());
                return true;
            } else {
                log.error("Brevo REST API email send failed to {} [Status: {}]: {}", toEmail, response.statusCode(), response.body());
                return false;
            }

        } catch (Exception e) {
            log.error("Exception occurred while sending email via Brevo REST API to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.equalsIgnoreCase("default") && !apiKey.equalsIgnoreCase("your_smtp_password");
    }

    @Override
    public String getProviderName() {
        return "Brevo REST API";
    }
}
