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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProjectRepository projectRepository;
    private final com.webliix.projects.repository.ProjectMemberRepository projectMemberRepository;
    private final com.webliix.security.repository.UserRepository userRepository;
    private final com.webliix.hr.employee.repository.EmployeeRepository employeeRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest req) {
        Invoice invoice = InvoiceMapper.toEntity(req);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isStaff = auth != null && auth.getAuthorities().stream().anyMatch(a -> {
            String role = a.getAuthority().toUpperCase();
            return role.contains("ADMIN") || role.contains("MANAGER") || role.contains("HR");
        });
        if (!isStaff && auth != null) {
            boolean isEmployee = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().toUpperCase().contains("EMPLOYEE"));
            if (isEmployee) {
                if (req.getProjectId() == null) {
                    throw new IllegalArgumentException("Employees must specify an assigned projectId to create an invoice/bill.");
                }
                String email = auth.getName().trim().toLowerCase();
                com.webliix.security.entity.User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
                java.util.Set<Long> candidateIds = new java.util.HashSet<>();
                candidateIds.add(user.getId());
                employeeRepository.findByUserId(user.getId()).ifPresent(e -> candidateIds.add(e.getId()));
                employeeRepository.findByEmail(email).ifPresent(e -> candidateIds.add(e.getId()));

                boolean isAssigned = projectMemberRepository.existsByProjectIdAndUserIdIn(req.getProjectId(), candidateIds);
                if (!isAssigned) {
                    throw new org.springframework.security.access.AccessDeniedException("You are not assigned to this project and cannot bill it.");
                }
            }
        }

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
            if (customer == null && project.getCustomer() != null) {
                customer = project.getCustomer();
                invoice.setCustomer(customer);
            }
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
                String recipientName = customer.getContactPerson() != null ? customer.getContactPerson() : customer.getCompanyName();
                String subject = "Invoice #" + saved.getInvoiceNumber() + " Generated – Webliix";
                String dueDateStr = saved.getDueDate() != null ? saved.getDueDate().toString() : "Due on receipt";
                String issueDateStr = saved.getIssueDate() != null ? saved.getIssueDate().toString() : "Today";

                StringBuilder itemsRows = new StringBuilder();
                if (saved.getItems() != null && !saved.getItems().isEmpty()) {
                    for (var item : saved.getItems()) {
                        itemsRows.append("<tr>")
                                 .append("<td style='padding: 8px; border-bottom: 1px solid #e2e8f0;'>").append(item.getItemName() != null ? item.getItemName() : "Service Item").append("</td>")
                                 .append("<td style='padding: 8px; border-bottom: 1px solid #e2e8f0; text-align: center;'>").append(item.getQuantity() != null ? item.getQuantity() : 1).append("</td>")
                                 .append("<td style='padding: 8px; border-bottom: 1px solid #e2e8f0; text-align: right;'>₹").append(item.getTotalPrice() != null ? item.getTotalPrice() : "0.00").append("</td>")
                                 .append("</tr>");
                    }
                } else {
                    itemsRows.append("<tr><td colspan='3' style='padding: 8px;'>Consulting & Engineering Services</td></tr>");
                }

                String html = "<!DOCTYPE html><html><body style='font-family: Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px; color: #1e293b;'>"
                        + "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.06); border: 1px solid #e2e8f0;'>"
                        + "  <div style='background-color: #0f172a; padding: 24px; text-align: center; color: #ffffff;'>"
                        + "    <h2 style='margin: 0; font-size: 22px; font-weight: 800;'>Webliix Invoice</h2>"
                        + "    <p style='margin: 6px 0 0; color: #94a3b8; font-size: 13px;'>Official Invoice Statement</p>"
                        + "  </div>"
                        + "  <div style='padding: 24px;'>"
                        + "    <p style='font-size: 15px; margin: 0 0 16px;'>Dear <strong>" + recipientName + "</strong>,</p>"
                        + "    <p style='font-size: 14px; color: #475569; margin: 0 0 20px; line-height: 1.5;'>A new invoice <strong>#" + saved.getInvoiceNumber() + "</strong> has been generated for your account. You can view, track, and download the official invoice directly from your client portal.</p>"
                        + "    <div style='background-color: #f1f5f9; border-radius: 8px; padding: 16px; margin-bottom: 20px;'>"
                        + "      <table style='width: 100%; font-size: 14px; border-collapse: collapse;'>"
                        + "        <tr><td style='color: #64748b; padding: 4px 0;'>Invoice Number:</td><td style='font-weight: 700; text-align: right;'>" + saved.getInvoiceNumber() + "</td></tr>"
                        + "        <tr><td style='color: #64748b; padding: 4px 0;'>Issue Date:</td><td style='font-weight: 600; text-align: right;'>" + issueDateStr + "</td></tr>"
                        + "        <tr><td style='color: #64748b; padding: 4px 0;'>Due Date:</td><td style='font-weight: 700; color: #d97706; text-align: right;'>" + dueDateStr + "</td></tr>"
                        + "        <tr><td style='color: #64748b; padding: 4px 0;'>Total Amount:</td><td style='font-weight: 800; font-size: 16px; color: #2563eb; text-align: right;'>₹" + saved.getTotalAmount() + "</td></tr>"
                        + "        <tr><td style='color: #64748b; padding: 4px 0;'>Status:</td><td style='font-weight: 700; text-align: right;'>" + saved.getStatus() + "</td></tr>"
                        + "      </table>"
                        + "    </div>"
                        + "    <h4 style='margin: 0 0 10px; font-size: 14px; color: #0f172a;'>Line Items Breakdown:</h4>"
                        + "    <table style='width: 100%; font-size: 13px; border-collapse: collapse; margin-bottom: 24px;'>"
                        + "      <tr style='background-color: #f8fafc; font-weight: 700; text-align: left;'>"
                        + "        <th style='padding: 8px; border-bottom: 2px solid #cbd5e1;'>Item</th>"
                        + "        <th style='padding: 8px; border-bottom: 2px solid #cbd5e1; text-align: center;'>Qty</th>"
                        + "        <th style='padding: 8px; border-bottom: 2px solid #cbd5e1; text-align: right;'>Amount</th>"
                        + "      </tr>"
                        + itemsRows.toString()
                        + "    </table>"
                        + "    <div style='text-align: center; margin: 28px 0 16px;'>"
                        + "      <a href='https://login.webliix.com/invoices' style='background-color: #2563eb; color: #ffffff; text-decoration: none; padding: 12px 28px; border-radius: 6px; font-weight: 700; font-size: 14px; display: inline-block;'>View & Download Invoice</a>"
                        + "    </div>"
                        + "    <p style='font-size: 13px; color: #94a3b8; text-align: center; margin: 0;'>Or log in to your Client Portal at <a href='https://login.webliix.com' style='color: #2563eb;'>login.webliix.com</a>.</p>"
                        + "  </div>"
                        + "  <div style='background-color: #f8fafc; padding: 16px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0;'>"
                        + "    <p style='margin: 0;'>Sent with care by <strong>Webliix Team</strong></p>"
                        + "  </div>"
                        + "</div></body></html>";

                emailService.sendAutomatedHtmlEmail(customer.getEmail(), subject, html);
            } catch (Exception ex) {
                log.warn("Could not dispatch automated invoice HTML email to {}: {}", customer.getEmail(), ex.getMessage());
            }
        }

        return InvoiceMapper.toResponse(saved);
    }

    @Override
    public Page<InvoiceResponse> getAllInvoices(Pageable pageable) {
        return getAllInvoices(null, null, pageable);
    }

    @Override
    public Page<InvoiceResponse> getAllInvoices(Long projectId, Long customerId, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isCustomer(auth)) {
            String email = auth != null ? auth.getName() : "";
            if (projectId != null) {
                Project project = projectRepository.findById(projectId).orElse(null);
                if (project == null || project.getCustomer() == null || !project.getCustomer().getEmail().equalsIgnoreCase(email)) {
                    throw new org.springframework.security.access.AccessDeniedException("Access denied: You do not own this project.");
                }
                return invoiceRepository.findByProjectId(projectId, pageable).map(InvoiceMapper::toResponse);
            }
            return invoiceRepository.findByCustomerEmail(email, pageable).map(InvoiceMapper::toResponse);
        }

        if (projectId != null) {
            return invoiceRepository.findByProjectId(projectId, pageable).map(InvoiceMapper::toResponse);
        }
        if (customerId != null) {
            return invoiceRepository.findByCustomerId(customerId, pageable).map(InvoiceMapper::toResponse);
        }
        return invoiceRepository.findAll(pageable).map(InvoiceMapper::toResponse);
    }

    @Override
    public InvoiceResponse getInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isCustomer(auth)) {
            String email = auth != null ? auth.getName() : "";
            if (invoice.getCustomer() == null || invoice.getCustomer().getEmail() == null || !invoice.getCustomer().getEmail().equalsIgnoreCase(email)) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied: You can only view your own customer invoices.");
            }
        }
        return InvoiceMapper.toResponse(invoice);
    }

    private boolean isCustomer(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> {
            String role = a.getAuthority().toUpperCase();
            return role.equals("ROLE_USER") || role.equals("USER") || role.equals("ROLE_CLIENT") || role.equals("CLIENT");
        });
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isCustomer(auth)) {
            String email = auth != null ? auth.getName() : "";
            return invoiceRepository.findByCustomerEmailAndInvoiceNumberContainingIgnoreCaseOrCustomerEmailAndProjectProjectNameContainingIgnoreCase(
                    email, keyword, email, keyword, pageable)
                    .map(InvoiceMapper::toResponse);
        }
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
