package com.webliix.crm.customer.service.impl;

import com.webliix.crm.customer.dto.CreateCustomerRequest;
import com.webliix.crm.customer.dto.CustomerResponse;
import com.webliix.crm.customer.entity.Customer;
import com.webliix.crm.customer.entity.CustomerContact;
import com.webliix.crm.customer.entity.CustomerNote;
import com.webliix.crm.customer.repository.CustomerContactRepository;
import com.webliix.crm.customer.repository.CustomerNoteRepository;
import com.webliix.crm.customer.repository.CustomerRepository;
import com.webliix.crm.customer.service.CustomerCodeGenerator;
import com.webliix.crm.customer.service.CustomerMapper;
import com.webliix.crm.customer.service.CustomerService;
import com.webliix.finance.dto.InvoiceResponse;
import com.webliix.finance.mapper.InvoiceMapper;
import com.webliix.finance.repository.InvoiceRepository;
import com.webliix.notifications.service.EmailService;
import com.webliix.projects.dto.ProjectResponse;
import com.webliix.projects.repository.ProjectRepository;
import com.webliix.projects.service.ProjectMapper;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerContactRepository contactRepository;
    private final CustomerNoteRepository noteRepository;
    private final ProjectRepository projectRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerCodeGenerator customerCodeGenerator;
    private final EmailService emailService;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        Customer customer = CustomerMapper.toEntity(request);
        customer.setCustomerCode(customerCodeGenerator.generateNextCustomerCode());
        if (customer.getCreatedAt() == null) {
            customer.setCreatedAt(LocalDateTime.now());
        }
        if (customer.getUpdatedAt() == null) {
            customer.setUpdatedAt(LocalDateTime.now());
        }
        Customer saved = customerRepository.save(customer);

        // Automated Welcome Email
        if (saved.getEmail() != null && !saved.getEmail().isBlank()) {
            try {
                String subject = "Welcome to Webliix – Your Client Account Details (" + saved.getCustomerCode() + ")";
                String body = "Hello " + (saved.getContactPerson() != null ? saved.getContactPerson() : saved.getCompanyName()) + ",\n\n"
                        + "Welcome to Webliix! Your client account has been successfully registered.\n\n"
                        + "Account Information:\n"
                        + "• Customer Code: " + saved.getCustomerCode() + "\n"
                        + "• Company Name: " + saved.getCompanyName() + "\n"
                        + "• Email: " + saved.getEmail() + "\n\n"
                        + "You can track your projects, invoices, and communications through our portal.\n\n"
                        + "Best regards,\nWebliix Operations Team\nhttps://webliix.in";
                emailService.sendEmail(saved.getEmail(), subject, body);
            } catch (Exception ex) {
                log.warn("Could not send automated welcome email to {}: {}", saved.getEmail(), ex.getMessage());
            }
        }

        return CustomerMapper.toResponse(saved);
    }

    @Override
    public CustomerResponse getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return CustomerMapper.toResponse(customer);
    }

    @Override
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(CustomerMapper::toResponse);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, CreateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        CustomerMapper.updateEntity(customer, request);
        customer.setUpdatedAt(LocalDateTime.now());
        Customer updated = customerRepository.save(customer);
        return CustomerMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    @Override
    public Page<CustomerResponse> searchCustomers(String keyword, Pageable pageable) {
        return customerRepository.findByCompanyNameContainingIgnoreCase(keyword, pageable)
                .map(CustomerMapper::toResponse);
    }

    @Override
    public List<ProjectResponse> getCustomerProjects(Long customerId) {
        return projectRepository.findByCustomerId(customerId).stream()
                .map(ProjectMapper::toResponse)
                .toList();
    }

    @Override
    public List<InvoiceResponse> getCustomerInvoices(Long customerId) {
        return invoiceRepository.findByCustomerId(customerId).stream()
                .map(InvoiceMapper::toResponse)
                .toList();
    }

    @Override
    public List<CustomerContact> getCustomerContacts(Long customerId) {
        return contactRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional
    public CustomerContact addCustomerContact(Long customerId, CustomerContact contact) {
        contact.setCustomerId(customerId);
        return contactRepository.save(contact);
    }

    @Override
    public List<CustomerNote> getCustomerNotes(Long customerId) {
        return noteRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Override
    @Transactional
    public CustomerNote addCustomerNote(Long customerId, CustomerNote note) {
        note.setCustomerId(customerId);
        note.setCreatedAt(LocalDateTime.now());
        return noteRepository.save(note);
    }
}
