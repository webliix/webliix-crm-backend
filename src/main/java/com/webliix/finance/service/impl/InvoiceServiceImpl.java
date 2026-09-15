package com.webliix.finance.service.impl;

import com.webliix.finance.dto.CreateInvoiceRequest;
import com.webliix.finance.dto.InvoiceDashboardResponse;
import com.webliix.finance.dto.InvoiceResponse;
import com.webliix.finance.entity.Invoice;
import com.webliix.finance.mapper.InvoiceMapper;
import com.webliix.finance.repository.InvoiceRepository;
import com.webliix.finance.service.InvoiceService;
import com.webliix.finance.util.InvoiceNumberGenerator;
import com.webliix.notifications.service.EmailService;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import com.webliix.crm.customer.entity.Customer;
import com.webliix.crm.customer.repository.CustomerRepository;
import com.webliix.projects.entity.Project;
import com.webliix.projects.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProjectRepository projectRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest req) {
        Invoice invoice = InvoiceMapper.toEntity(req);

        Customer customer = null;
        if (req.getCustomerId() != null) {
            customer = customerRepository.findById(req.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            invoice.setCustomer(customer);
        }
        if (req.getProjectId() != null) {
            Project project = projectRepository.findById(req.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
            invoice.setProject(project);
        }

        String prefix = InvoiceNumberGenerator.currentPrefix();
        String invoiceNumber = invoiceRepository.findTopByInvoiceNumberStartingWithOrderByIdDesc(prefix)
                .map(i -> InvoiceNumberGenerator.next(i.getInvoiceNumber()))
                .orElse(prefix + "000001");
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        if (invoice.getPaidAmount() == null) {
            invoice.setPaidAmount(BigDecimal.ZERO);
        }
        if (invoice.getPendingAmount() == null) {
            invoice.setPendingAmount(invoice.getTotalAmount());
        }
        Invoice saved = invoiceRepository.save(invoice);

        // Automated Invoice Notification Email to Customer
        if (customer != null && customer.getEmail() != null && !customer.getEmail().isBlank()) {
            try {
                String subject = "Invoice #" + saved.getInvoiceNumber() + " Generated – Webliix Hub";
                String body = "Dear " + (customer.getContactPerson() != null ? customer.getContactPerson() : customer.getCompanyName()) + ",\n\n"
                        + "A new invoice #" + saved.getInvoiceNumber() + " has been generated for your account.\n\n"
                        + "Invoice Summary:\n"
                        + "• Invoice Number: " + saved.getInvoiceNumber() + "\n"
                        + "• Total Amount: " + saved.getTotalAmount() + "\n"
                        + "• Issue Date: " + (saved.getIssueDate() != null ? saved.getIssueDate() : "Today") + "\n"
                        + "• Due Date: " + (saved.getDueDate() != null ? saved.getDueDate() : "Upon Receipt") + "\n"
                        + "• Status: " + saved.getStatus() + "\n\n"
                        + "Please review the invoice and complete payment before the due date.\n\n"
                        + "Best regards,\nWebliix Finance Team\nhttps://webliix.in";
                emailService.sendEmail(customer.getEmail(), subject, body);
            } catch (Exception ex) {
                log.warn("Could not dispatch automated invoice email to {}: {}", customer.getEmail(), ex.getMessage());
            }
        }

        return InvoiceMapper.toResponse(saved);
    }

    @Override
    public Page<InvoiceResponse> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable).map(InvoiceMapper::toResponse);
    }

    @Override
    public InvoiceResponse getInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return InvoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse updateInvoice(Long id, CreateInvoiceRequest req) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        Invoice updated = InvoiceMapper.toEntity(req);
        updated.setId(invoice.getId());
        updated.setInvoiceNumber(invoice.getInvoiceNumber());
        updated.setCustomer(invoice.getCustomer());
        updated.setProject(invoice.getProject());
        updated.setStatus(invoice.getStatus());
        updated.setPaidAmount(invoice.getPaidAmount());
        updated.setPendingAmount(invoice.getPendingAmount());
        updated.setCreatedAt(invoice.getCreatedAt());
        updated.setUpdatedAt(LocalDateTime.now());
        if (updated.getPaidAmount() == null) {
            updated.setPaidAmount(BigDecimal.ZERO);
        }
        if (updated.getPendingAmount() == null && updated.getTotalAmount() != null) {
            updated.setPendingAmount(updated.getTotalAmount().subtract(updated.getPaidAmount()));
        }
        Invoice saved = invoiceRepository.save(updated);
        return InvoiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Invoice not found");
        }
        invoiceRepository.deleteById(id);
    }

    @Override
    public Page<InvoiceResponse> searchInvoices(String keyword, Pageable pageable) {
        return invoiceRepository.findByInvoiceNumberContainingIgnoreCaseOrCustomerCompanyNameContainingIgnoreCaseOrProjectProjectNameContainingIgnoreCase(
                keyword, keyword, keyword, pageable)
                .map(InvoiceMapper::toResponse);
    }

    @Override
    public InvoiceDashboardResponse getDashboard() {
        long total = invoiceRepository.count();
        long paid = invoiceRepository.findAll().stream()
                .filter(i -> i.getStatus() == com.webliix.finance.enums.InvoiceStatus.PAID)
                .count();
        long overdue = invoiceRepository.findAll().stream()
                .filter(i -> i.getStatus() == com.webliix.finance.enums.InvoiceStatus.OVERDUE)
                .count();
        long pending = invoiceRepository.findAll().stream()
                .filter(i -> i.getStatus() != com.webliix.finance.enums.InvoiceStatus.PAID)
                .count();
        BigDecimal revenue = invoiceRepository.findAll().stream()
                .map(Invoice::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        InvoiceDashboardResponse response = new InvoiceDashboardResponse();
        response.setTotalInvoices(total);
        response.setPaidInvoices(paid);
        response.setOverdueInvoices(overdue);
        response.setPendingInvoices(pending);
        response.setTotalRevenue(revenue);
        return response;
    }
}
