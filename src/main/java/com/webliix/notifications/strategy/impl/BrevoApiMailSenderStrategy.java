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
            boolean sent = doSend(fromEmail, fromName, toEmail, subject, content, isHtml);
            if (sent) return true;

            // If initial send failed and fromEmail is not contact@webliix.com, retry with verified contact@webliix.com
            if (!"contact@webliix.com".equalsIgnoreCase(fromEmail)) {
                log.info("Retrying email delivery with verified sender contact@webliix.com for recipient: {}", toEmail);
                sent = doSend("contact@webliix.com", "Webliix", toEmail, subject, content, isHtml);
                if (sent) return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Exception occurred while sending email via Brevo REST API to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    private boolean doSend(String fromEmail, String fromName, String toEmail, String subject, String content, boolean isHtml) {
        try {
            Map<String, Object> payload = new HashMap<>();

            Map<String, String> sender = new HashMap<>();
            sender.put("email", fromEmail != null && !fromEmail.isBlank() ? fromEmail : "contact@webliix.com");
            sender.put("name", fromName != null && !fromName.isBlank() ? fromName : "Webliix");
            payload.put("sender", sender);

            Map<String, String> recipient = new HashMap<>();
            recipient.put("email", toEmail.trim().toLowerCase());
            payload.put("to", List.of(recipient));

            Map<String, String> replyTo = new HashMap<>();
            replyTo.put("email", "contact@webliix.com");
            replyTo.put("name", "Webliix Support");
            payload.put("replyTo", replyTo);

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
            }

            String responseBody = response.body() != null ? response.body().toLowerCase() : "";
            log.warn("Brevo REST API email send rejected for {} [Status: {}]: {}", toEmail, response.statusCode(), response.body());

            // If recipient is blacklisted, not subscribed, or blocked in Brevo, trigger automatic unblock and resubscription
            if (responseBody.contains("blacklist") || responseBody.contains("subscribed") || responseBody.contains("blocked") || responseBody.contains("unauthorized")) {
                log.info("Recipient {} flagged as blacklisted/unsubscribed by Brevo. Unblocking and resubscribing...", toEmail);
                boolean unblocked = unblockRecipient(toEmail);
                if (unblocked) {
                    try {
                        Thread.sleep(400); // Give Brevo contact cache brief sync window
                    } catch (InterruptedException ignored) {}

                    HttpResponse<String> retryResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (retryResponse.statusCode() >= 200 && retryResponse.statusCode() < 300) {
                        log.info("Successfully delivered email to {} via Brevo REST API after unblocking!", toEmail);
                        return true;
                    } else {
                        log.error("Brevo retry after unblocking still failed to {} [Status: {}]: {}", toEmail, retryResponse.statusCode(), retryResponse.body());
                    }
                }
            }

            return false;
        } catch (Exception e) {
            log.error("doSend exception for {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean unblockRecipient(String email) {
        if (!isAvailable() || email == null || email.isBlank()) {
            return false;
        }

        String cleanEmail = email.trim().toLowerCase();
        try {
            String encodedEmail = java.net.URLEncoder.encode(cleanEmail, java.nio.charset.StandardCharsets.UTF_8);

            // 1. Remove from transactional blocklist (DELETE /v3/smtp/blockedContacts/{email})
            HttpRequest delReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/blockedContacts/" + encodedEmail))
                    .timeout(Duration.ofSeconds(6))
                    .header("accept", "application/json")
                    .header("api-key", apiKey)
                    .DELETE()
                    .build();
            HttpResponse<String> delRes = httpClient.send(delReq, HttpResponse.BodyHandlers.ofString());
            log.info("Brevo unblock blockedContacts for {}: Status {}", cleanEmail, delRes.statusCode());

            // 2. Ensure contact exists and emailBlacklisted is false (POST /v3/contacts)
            Map<String, Object> contactPayload = new HashMap<>();
            contactPayload.put("email", cleanEmail);
            contactPayload.put("emailBlacklisted", false);
            contactPayload.put("smsBlacklisted", false);
            contactPayload.put("updateEnabled", true);

            HttpRequest createReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/contacts"))
                    .timeout(Duration.ofSeconds(6))
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .header("api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(contactPayload)))
                    .build();
            HttpResponse<String> createRes = httpClient.send(createReq, HttpResponse.BodyHandlers.ofString());
            log.info("Brevo upsert subscribed contact for {}: Status {}", cleanEmail, createRes.statusCode());

            // 3. Explicitly update contact in case it already existed (PUT /v3/contacts/{email})
            Map<String, Object> updatePayload = new HashMap<>();
            updatePayload.put("emailBlacklisted", false);
            updatePayload.put("smsBlacklisted", false);

            HttpRequest updateReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/contacts/" + encodedEmail))
                    .timeout(Duration.ofSeconds(6))
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .header("api-key", apiKey)
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(updatePayload)))
                    .build();
            HttpResponse<String> updateRes = httpClient.send(updateReq, HttpResponse.BodyHandlers.ofString());
            log.info("Brevo clear blacklist status for {}: Status {}", cleanEmail, updateRes.statusCode());

            return true;
        } catch (Exception e) {
            log.warn("Failed to unblock/subscribe contact in Brevo for {}: {}", cleanEmail, e.getMessage());
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
