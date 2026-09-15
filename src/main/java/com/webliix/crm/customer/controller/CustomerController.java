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

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@RequestBody CreateCustomerRequest request) {
        CustomerResponse res = customerService.createCustomer(request);
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer created successfully")
                .data(res)
                .build());
    }

    @GetMapping
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
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable Long id) {
        CustomerResponse res = customerService.getCustomer(id);
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer fetched")
                .data(res)
                .build());
    }

    @PutMapping("/{id}")
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
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Customer deleted successfully")
                .build());
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<CustomerStatisticsResponse>> getStatistics() {
        CustomerStatisticsResponse res = statisticsService.getStatistics();
        return ResponseEntity.ok(ApiResponse.<CustomerStatisticsResponse>builder()
                .success(true)
                .message("Customer statistics fetched")
                .data(res)
                .build());
    }

    @GetMapping("/{id}/projects")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getCustomerProjects(@PathVariable Long id) {
        List<ProjectResponse> res = customerService.getCustomerProjects(id);
        return ResponseEntity.ok(ApiResponse.<List<ProjectResponse>>builder()
                .success(true)
                .message("Customer projects fetched")
                .data(res)
                .build());
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getCustomerInvoices(@PathVariable Long id) {
        List<InvoiceResponse> res = customerService.getCustomerInvoices(id);
        return ResponseEntity.ok(ApiResponse.<List<InvoiceResponse>>builder()
                .success(true)
                .message("Customer invoices fetched")
                .data(res)
                .build());
    }

    @GetMapping("/{id}/contacts")
    public ResponseEntity<ApiResponse<List<CustomerContact>>> getCustomerContacts(@PathVariable Long id) {
        List<CustomerContact> res = customerService.getCustomerContacts(id);
        return ResponseEntity.ok(ApiResponse.<List<CustomerContact>>builder()
                .success(true)
                .message("Customer contacts fetched")
                .data(res)
                .build());
    }

    @PostMapping("/{id}/contacts")
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
    public ResponseEntity<ApiResponse<List<CustomerNote>>> getCustomerNotes(@PathVariable Long id) {
        List<CustomerNote> res = customerService.getCustomerNotes(id);
        return ResponseEntity.ok(ApiResponse.<List<CustomerNote>>builder()
                .success(true)
                .message("Customer notes fetched")
                .data(res)
                .build());
    }

    @PostMapping("/{id}/notes")
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
}
