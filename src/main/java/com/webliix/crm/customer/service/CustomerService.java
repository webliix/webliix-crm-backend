package com.webliix.crm.customer.service;

import com.webliix.crm.customer.dto.CreateCustomerRequest;
import com.webliix.crm.customer.dto.CustomerResponse;
import com.webliix.crm.customer.entity.CustomerContact;
import com.webliix.crm.customer.entity.CustomerNote;
import com.webliix.finance.dto.InvoiceResponse;
import com.webliix.projects.dto.ProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {

    CustomerResponse createCustomer(CreateCustomerRequest request);

    CustomerResponse getCustomer(Long id);

    Page<CustomerResponse> getAllCustomers(Pageable pageable);

    CustomerResponse updateCustomer(Long id, CreateCustomerRequest request);

    void deleteCustomer(Long id);

    Page<CustomerResponse> searchCustomers(String keyword, Pageable pageable);

    List<ProjectResponse> getCustomerProjects(Long customerId);

    List<InvoiceResponse> getCustomerInvoices(Long customerId);

    List<CustomerContact> getCustomerContacts(Long customerId);

    CustomerContact addCustomerContact(Long customerId, CustomerContact contact);

    List<CustomerNote> getCustomerNotes(Long customerId);

    CustomerNote addCustomerNote(Long customerId, CustomerNote note);
}
