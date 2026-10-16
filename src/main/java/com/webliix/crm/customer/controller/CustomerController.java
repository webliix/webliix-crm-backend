package com.webliix.crm.customer.controller;

import com.webliix.crm.customer.dto.CreateCustomerRequest;
import com.webliix.crm.customer.dto.CustomerResponse;
import com.webliix.crm.customer.dto.CustomerStatisticsResponse;
import com.webliix.crm.customer.entity.CustomerContact;
import com.webliix.crm.customer.entity.CustomerNote;
import com.webliix.crm.customer.service.CustomerService;
import com.webliix.crm.customer.service.CustomerStatisticsService;
import com.webliix.finance.dto.InvoiceResponse;
import com.webliix.projects.dto.ProjectResponse;
import com.webliix.security.annotation.RequirePermission;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerStatisticsService statisticsService;
    private final com.webliix.notifications.service.EmailService emailService;

    @PostMapping
    @RequirePermission("CUSTOMERS_CREATE")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@RequestBody CreateCustomerRequest request) {
        CustomerResponse res = customerService.createCustomer(request);
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer created successfully")
                .data(res)
                .build());
    }

    @GetMapping
    @RequirePermission("CUSTOMERS_VIEW")
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerResponse> res = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<CustomerResponse>>builder()
                .success(true)
                .message("Customers fetched")
                .data(res)
                .build());
    }

    @GetMapping("/search")
    @RequirePermission("CUSTOMERS_VIEW")
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> searchCustomers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerResponse> res = customerService.searchCustomers(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<CustomerResponse>>builder()
                .success(true)
                .message("Search results")
                .data(res)
                .build());
    }

    @GetMapping("/{id}")
    @RequirePermission("CUSTOMERS_VIEW")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable Long id) {
        CustomerResponse res = customerService.getCustomer(id);
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer fetched")
                .data(res)
                .build());
    }

    @PutMapping("/{id}")
    @RequirePermission("CUSTOMERS_EDIT")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long id,
            @RequestBody CreateCustomerRequest request
    ) {
        CustomerResponse res = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer updated successfully")
                .data(res)
                .build());
    }

    @DeleteMapping("/{id}")
    @RequirePermission("CUSTOMERS_DELETE")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Customer deleted successfully")
                .build());
    }

    @GetMapping("/statistics")
    @RequirePermission("CUSTOMERS_VIEW")
    public ResponseEntity<ApiResponse<CustomerStatisticsResponse>> getStatistics() {
        CustomerStatisticsResponse res = statisticsService.getStatistics();
        return ResponseEntity.ok(ApiResponse.<CustomerStatisticsResponse>builder()
                .success(true)
                .message("Customer statistics fetched")
                .data(res)
                .build());
    }

    @GetMapping("/{id}/projects")
    @RequirePermission("CUSTOMERS_VIEW")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getCustomerProjects(@PathVariable Long id) {
        List<ProjectResponse> res = customerService.getCustomerProjects(id);
        return ResponseEntity.ok(ApiResponse.<List<ProjectResponse>>builder()
                .success(true)
                .message("Customer projects fetched")
                .data(res)
                .build());
    }

    @GetMapping("/{id}/invoices")
    @RequirePermission("CUSTOMERS_VIEW")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getCustomerInvoices(@PathVariable Long id) {
        List<InvoiceResponse> res = customerService.getCustomerInvoices(id);
        return ResponseEntity.ok(ApiResponse.<List<InvoiceResponse>>builder()
                .success(true)
                .message("Customer invoices fetched")
                .data(res)
                .build());
    }

    @GetMapping("/{id}/contacts")
    @RequirePermission("CUSTOMERS_VIEW")
    public ResponseEntity<ApiResponse<List<CustomerContact>>> getCustomerContacts(@PathVariable Long id) {
        List<CustomerContact> res = customerService.getCustomerContacts(id);
        return ResponseEntity.ok(ApiResponse.<List<CustomerContact>>builder()
                .success(true)
                .message("Customer contacts fetched")
                .data(res)
                .build());
    }

    @PostMapping("/{id}/contacts")
    @RequirePermission("CUSTOMERS_EDIT")
    public ResponseEntity<ApiResponse<CustomerContact>> addCustomerContact(
            @PathVariable Long id,
            @RequestBody CustomerContact contact
    ) {
        CustomerContact res = customerService.addCustomerContact(id, contact);
        return ResponseEntity.ok(ApiResponse.<CustomerContact>builder()
                .success(true)
                .message("Customer contact added")
                .data(res)
                .build());
    }

    @GetMapping("/{id}/notes")
    @RequirePermission("CUSTOMERS_VIEW")
    public ResponseEntity<ApiResponse<List<CustomerNote>>> getCustomerNotes(@PathVariable Long id) {
        List<CustomerNote> res = customerService.getCustomerNotes(id);
        return ResponseEntity.ok(ApiResponse.<List<CustomerNote>>builder()
                .success(true)
                .message("Customer notes fetched")
                .data(res)
                .build());
    }

    @PostMapping("/{id}/notes")
    @RequirePermission("CUSTOMERS_EDIT")
    public ResponseEntity<ApiResponse<CustomerNote>> addCustomerNote(
            @PathVariable Long id,
            @RequestBody CustomerNote note
    ) {
        CustomerNote res = customerService.addCustomerNote(id, note);
        return ResponseEntity.ok(ApiResponse.<CustomerNote>builder()
                .success(true)
                .message("Customer note added")
                .data(res)
                .build());
    }

    @PostMapping("/{id}/send-email")
    @RequirePermission(anyOf = {"CUSTOMERS_EDIT", "CUSTOMERS_VIEW"})
    public ResponseEntity<ApiResponse<Void>> sendEmailToCustomer(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody com.webliix.crm.customer.dto.SendCustomerEmailRequest request,
            org.springframework.security.core.Authentication auth
    ) {
        CustomerResponse customer = customerService.getCustomer(id);
        String recipient = request.getRecipientEmail() != null && !request.getRecipientEmail().isBlank()
                ? request.getRecipientEmail().trim()
                : customer.getEmail();

        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("Customer does not have a registered email address.");
        }

        String customerName = customer.getContactPerson() != null && !customer.getContactPerson().isBlank()
                ? customer.getContactPerson()
                : (customer.getCompanyName() != null ? customer.getCompanyName() : "Valued Client");

        String html = buildCustomerEmailHtml(request.getSubject(), customerName, request.getMessage());
        emailService.sendHtmlEmail(recipient, request.getSubject(), html);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Email sent successfully to " + recipient)
                .build());
    }

    private String buildCustomerEmailHtml(String subject, String customerName, String message) {
        String escapedMsg = message.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>");
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>%s</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8fafc; color: #1e293b;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #f8fafc; padding: 40px 15px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width: 560px; background-color: #ffffff; border-radius: 12px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);">
                                    <tr>
                                        <td style="background-color: #0f172a; padding: 24px 32px; text-align: center;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 24px; font-weight: 800; letter-spacing: -0.5px;">Webliix <span style="color: #6366f1;">Hub</span></h1>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 32px;">
                                            <h2 style="margin: 0 0 16px 0; color: #0f172a; font-size: 20px; font-weight: 700;">%s</h2>
                                            <p style="margin: 0 0 16px 0; font-size: 15px; color: #334155;">Dear <strong>%s</strong>,</p>
                                            <div style="font-size: 14px; line-height: 1.7; color: #475569; margin: 0 0 24px 0;">
                                                %s
                                            </div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="background-color: #f8fafc; padding: 20px 32px; border-top: 1px solid #e2e8f0; text-align: center;">
                                            <p style="margin: 0 0 4px 0; color: #64748b; font-size: 12px; font-weight: 600;">Webliix Hub • Enterprise Client & Management Platform</p>
                                            <p style="margin: 0; color: #94a3b8; font-size: 11px;">&lt; Your Success is our Code /&gt; • contact@webliix.com</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(subject, subject, customerName, escapedMsg);
    }
}
