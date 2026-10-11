package com.webliix.notifications.service;

import com.webliix.notifications.dto.*;

import java.util.List;

public interface EmailTemplateService {
    List<EmailTemplateResponse> getAllTemplates();
    EmailTemplateResponse getTemplateById(Long id);
    EmailTemplateResponse getTemplateByKey(String templateKey);
    EmailTemplateResponse updateTemplate(Long id, UpdateEmailTemplateRequest request);
    EmailTemplateResponse toggleTemplate(Long id);
    String previewTemplate(Long id, PreviewEmailTemplateRequest request);
    boolean testSendTemplate(Long id, TestSendEmailTemplateRequest request);
    EmailTemplateResponse resetTemplateToDefault(Long id);

    NotificationRulesResponse getNotificationRules();
    NotificationRulesResponse updateNotificationRules(UpdateNotificationRulesRequest request);
}
