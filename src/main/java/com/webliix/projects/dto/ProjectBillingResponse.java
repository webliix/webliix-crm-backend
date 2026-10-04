package com.webliix.projects.dto;

import com.webliix.finance.dto.InvoiceResponse;
import com.webliix.hr.paymentsubmission.dto.PaymentSubmissionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectBillingResponse {

    private Long projectId;
    private String projectCode;
    private String projectName;
    private Long customerId;
    private String customerName;
    private String customerCompanyName;
    private BigDecimal budget;
    private BigDecimal totalBilled;
    private BigDecimal totalPaid;
    private BigDecimal pendingDueOnInvoices;
    private BigDecimal remainingProjectBalance;
    private BigDecimal unbilledContractAmount;
    private List<InvoiceResponse> invoices;
    private List<PaymentSubmissionResponse> paymentSubmissions;
}
