package com.webliix.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewEmailTemplateRequest {
    private String subject;
    private String htmlContent;
    private Map<String, String> variables;
}
