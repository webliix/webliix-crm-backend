package com.webliix.hr.worklog.dto;

import lombok.Data;

@Data
public class WorkLogReviewRequest {
    private String status; // APPROVED or REJECTED
    private String reviewNotes;
}
