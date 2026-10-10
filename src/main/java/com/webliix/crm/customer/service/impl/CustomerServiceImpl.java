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
    private final com.webliix.security.repository.UserRepository userRepository;
    private final com.webliix.security.repository.RoleRepository roleRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

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

        // Automated Client Portal Credential Generation & Welcome Email
        provisionClientPortalAccount(saved, request.getPassword());

        return CustomerMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void provisionClientPortalAccount(Customer saved, String customPassword) {
        if (saved == null || saved.getEmail() == null || saved.getEmail().isBlank()) {
            return;
        }

        try {
            String rawEmail = saved.getEmail().trim().toLowerCase();
            final String chosenPassword = (customPassword != null && !customPassword.isBlank())
                    ? customPassword.trim()
                    : ("Webliix#" + ((int)(Math.random() * 899999) + 100000));
            String passwordForEmail = chosenPassword;

            if (!userRepository.existsByEmail(rawEmail)) {
                com.webliix.security.entity.Role clientRole = roleRepository.findByName("USER")
                        .orElseGet(() -> roleRepository.findByName("ROLE_USER")
                        .orElseGet(() -> roleRepository.findByName("ROLE_CLIENT")
                        .orElseGet(() -> roleRepository.save(com.webliix.security.entity.Role.builder().name("USER").build()))));

                String name = saved.getContactPerson() != null && !saved.getContactPerson().isBlank()
                        ? saved.getContactPerson()
                        : (saved.getCompanyName() != null ? saved.getCompanyName() : "Client");

                com.webliix.security.entity.User user = com.webliix.security.entity.User.builder()
                        .firstName(name)
                        .lastName("Client")
                        .email(rawEmail)
                        .password(passwordEncoder.encode(chosenPassword))
                        .phone(saved.getPhone())
                        .enabled(true)
                        .emailVerified(true)
                        .roles(new java.util.HashSet<>(java.util.Set.of(clientRole)))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                userRepository.save(user);
                log.info("Provisioned new client portal User account for {}", rawEmail);
            } else {
                if (customPassword != null && !customPassword.isBlank()) {
                    userRepository.findByEmail(rawEmail).ifPresent(existingUser -> {
                        existingUser.setPassword(passwordEncoder.encode(chosenPassword));
                        existingUser.setUpdatedAt(LocalDateTime.now());
                        userRepository.save(existingUser);
                        log.info("Updated existing client password during onboarding for {}", rawEmail);
                    });
                } else {
                    passwordForEmail = "[Existing Account Password / Use Reset Link]";
                }
            }

            String subject = "Welcome to Webliix Client Portal – Account Credentials (" + (saved.getCustomerCode() != null ? saved.getCustomerCode() : "ACC") + ")";
            String customerName = saved.getContactPerson() != null && !saved.getContactPerson().isBlank()
                    ? saved.getContactPerson()
                    : (saved.getCompanyName() != null ? saved.getCompanyName() : "Valued Client");

            String htmlBody = null;
            try (java.io.InputStream is = getClass().getResourceAsStream("/templates/emails/customer-welcome.html")) {
                if (is != null) {
                    String template = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    htmlBody = template
                            .replace("{{NAME}}", customerName)
                            .replace("{{CUSTOMER_CODE}}", saved.getCustomerCode() != null ? saved.getCustomerCode() : "N/A")
                            .replace("{{EMAIL}}", rawEmail)
                            .replace("{{PASSWORD}}", passwordForEmail);
                }
            } catch (Exception ex) {
                log.warn("Could not load customer-welcome.html template: {}", ex.getMessage());
            }

            if (htmlBody != null) {
                emailService.sendAutomatedHtmlEmail(rawEmail, subject, htmlBody);
            } else {
                String body = "Hello " + customerName + ",\n\n"
                        + "Welcome to Webliix! Your client portal account has been created successfully.\n\n"
                        + "Client Portal Login Credentials:\n"
                        + "• Portal URL: https://login.webliix.com\n"
                        + "• Email: " + rawEmail + "\n"
                        + "• Password: " + passwordForEmail + "\n\n"
                        + "Log in at https://login.webliix.com to view project progress, milestone timelines, billing & invoices, and submit project updates or instructions.\n\n"
                        + "Best regards,\nWebliix Team\ncontact@webliix.com";
                emailService.sendEmail(rawEmail, subject, body);
            }
        } catch (Exception ex) {
            log.warn("Could not provision or send automated welcome credential email to {}: {}", saved.getEmail(), ex.getMessage());
        }
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
