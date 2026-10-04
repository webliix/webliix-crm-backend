package com.webliix.hr.paymentsubmission.dto;

import lombok.Data;

@Data
public class PaymentSubmissionReviewRequest {
    private String status; // APPROVED or REJECTED
    private String reviewNotes;
}
