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
    private final com.webliix.notifications.service.EmailTemplateService emailTemplateService;

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

    @GetMapping("/{id:[0-9]+}")
    @RequirePermission("CUSTOMERS_VIEW")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable Long id) {
        CustomerResponse res = customerService.getCustomer(id);
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer fetched")
                .data(res)
                .build());
    }

    @PutMapping("/{id:[0-9]+}")
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

    @DeleteMapping("/{id:[0-9]+}")
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

    @GetMapping("/{id:[0-9]+}/projects")
    @RequirePermission("CUSTOMERS_VIEW")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getCustomerProjects(@PathVariable Long id) {
        List<ProjectResponse> res = customerService.getCustomerProjects(id);
        return ResponseEntity.ok(ApiResponse.<List<ProjectResponse>>builder()
                .success(true)
                .message("Customer projects fetched")
                .data(res)
                .build());
    }

    @GetMapping("/{id:[0-9]+}/invoices")
    @RequirePermission("CUSTOMERS_VIEW")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getCustomerInvoices(@PathVariable Long id) {
        List<InvoiceResponse> res = customerService.getCustomerInvoices(id);
        return ResponseEntity.ok(ApiResponse.<List<InvoiceResponse>>builder()
                .success(true)
                .message("Customer invoices fetched")
                .data(res)
                .build());
    }

    @GetMapping("/{id:[0-9]+}/contacts")
    @RequirePermission("CUSTOMERS_VIEW")
    public ResponseEntity<ApiResponse<List<CustomerContact>>> getCustomerContacts(@PathVariable Long id) {
        List<CustomerContact> res = customerService.getCustomerContacts(id);
        return ResponseEntity.ok(ApiResponse.<List<CustomerContact>>builder()
                .success(true)
                .message("Customer contacts fetched")
                .data(res)
                .build());
    }

    @PostMapping("/{id:[0-9]+}/contacts")
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

    @GetMapping("/{id:[0-9]+}/notes")
    @RequirePermission("CUSTOMERS_VIEW")
    public ResponseEntity<ApiResponse<List<CustomerNote>>> getCustomerNotes(@PathVariable Long id) {
        List<CustomerNote> res = customerService.getCustomerNotes(id);
        return ResponseEntity.ok(ApiResponse.<List<CustomerNote>>builder()
                .success(true)
                .message("Customer notes fetched")
                .data(res)
                .build());
    }

    @PostMapping("/{id:[0-9]+}/notes")
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

    @PostMapping("/{id:[0-9]+}/send-email")
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

        java.util.Map<String, String> vars = new java.util.HashMap<>();
        vars.put("CLIENT_NAME", customerName);
        vars.put("COMPANY_NAME", customer.getCompanyName() != null ? customer.getCompanyName() : customerName);
        vars.put("SUBJECT", request.getSubject());
        vars.put("MESSAGE", request.getMessage());
        vars.put("CTA_TEXT", "Access Client Portal");
        vars.put("CTA_URL", "https://login.webliix.com");

        String html = emailTemplateService.renderTemplateWithLayout("client-direct-message", vars);
        emailService.sendCustomHtmlEmail(recipient, request.getSubject(), html);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Email sent successfully to " + recipient)
                .build());
    }

    @PostMapping("/broadcast-email")
    @RequirePermission(anyOf = {"CUSTOMERS_EDIT", "CUSTOMERS_VIEW", "DASHBOARD_VIEW"})
    public ResponseEntity<ApiResponse<Integer>> broadcastEmailToAllClients(
            @jakarta.validation.Valid @RequestBody com.webliix.crm.customer.dto.BroadcastCustomerEmailRequest request,
            org.springframework.security.core.Authentication auth
    ) {
        int count = customerService.broadcastEmailToAllCustomers(request);
        return ResponseEntity.ok(ApiResponse.<Integer>builder()
                .success(true)
                .message("Broadcast email successfully dispatched to " + count + " client accounts.")
                .data(count)
                .build());
    }
}
