package com.webliix.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditDashboardResponse {
    private long todayActivities;
    private long failedActivities;
    private long loginsToday;
    private long exportsToday;
}
