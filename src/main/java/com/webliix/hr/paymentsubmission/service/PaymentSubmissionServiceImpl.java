package com.webliix.hr.paymentsubmission.service;

import com.webliix.audit.entity.AuditAction;
import com.webliix.audit.entity.AuditModule;
import com.webliix.audit.service.AuditService;
import com.webliix.crm.customer.entity.Customer;
import com.webliix.crm.customer.repository.CustomerRepository;
import com.webliix.finance.entity.Invoice;
import com.webliix.finance.repository.InvoiceRepository;
import com.webliix.hr.employee.entity.Employee;
import com.webliix.hr.employee.repository.EmployeeRepository;
import com.webliix.hr.paymentsubmission.dto.PaymentSubmissionRequest;
import com.webliix.hr.paymentsubmission.dto.PaymentSubmissionResponse;
import com.webliix.hr.paymentsubmission.dto.PaymentSubmissionReviewRequest;
import com.webliix.hr.paymentsubmission.entity.PaymentSubmission;
import com.webliix.hr.paymentsubmission.repository.PaymentSubmissionRepository;
import com.webliix.notifications.dto.CreateNotificationRequest;
import com.webliix.notifications.enums.NotificationChannel;
import com.webliix.notifications.service.NotificationService;
import com.webliix.projects.entity.Project;
import com.webliix.projects.repository.ProjectMemberRepository;
import com.webliix.projects.repository.ProjectRepository;
import com.webliix.security.entity.User;
import com.webliix.security.repository.UserRepository;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSubmissionServiceImpl implements PaymentSubmissionService {

    private final PaymentSubmissionRepository paymentSubmissionRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final com.webliix.notifications.service.EmailService emailService;
    private final com.webliix.finance.payment.repository.PaymentRepository paymentRepository;

    private Employee resolveEmployeeFromAuth(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("Authentication required.");
        }
        String email = auth.getName().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return employeeRepository.findByUserId(user.getId())
                .or(() -> employeeRepository.findByEmail(email).map(emp -> {
                    if (emp.getUser() == null) {
                        emp.setUser(user);
                        return employeeRepository.save(emp);
                    }
                    return emp;
                }))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No employee profile linked to your account. Contact your administrator."));
    }

    @Override
    @Transactional
    public PaymentSubmissionResponse submitPayment(PaymentSubmissionRequest request, Authentication auth) {
        Employee employee = resolveEmployeeFromAuth(auth);

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));
            // Validate that the employee is a member of this project
            boolean isMember = projectMemberRepository.findByProjectId(project.getId())
                    .stream()
                    .anyMatch(m -> m.getUserId() != null && m.getUserId().equals(employee.getUser().getId()));
            if (!isMember) {
                throw new AccessDeniedException("You are not assigned to project: " + project.getProjectName());
            }
        }

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
        } else if (project != null && project.getCustomer() != null) {
            customer = project.getCustomer();
        }

        Invoice invoice = null;
        if (request.getLinkedInvoiceId() != null) {
            invoice = invoiceRepository.findById(request.getLinkedInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + request.getLinkedInvoiceId()));
        }

        // CRITICAL BUSINESS RULE: Status is ALWAYS initialized to PENDING_REVIEW.
        // It does NOT update invoices, alter balances, or mark as paid.
        PaymentSubmission submission = PaymentSubmission.builder()
                .employee(employee)
                .project(project)
                .customer(customer)
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency().toUpperCase() : "USD")
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now())
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(request.getReferenceNumber())
                .payerName(request.getPayerName())
                .receiverDetails(request.getReceiverDetails())
                .notes(request.getNotes())
                .linkedInvoice(invoice)
                .status("PENDING_REVIEW")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PaymentSubmission saved = paymentSubmissionRepository.save(submission);

        auditService.record(AuditAction.CREATE, AuditModule.FINANCE, "PaymentSubmission",
                saved.getId().toString(), null, saved, "SUCCESS", null);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentSubmissionResponse> getMySubmissions(Pageable pageable, Authentication auth) {
        Employee employee = resolveEmployeeFromAuth(auth);
        return paymentSubmissionRepository.findByEmployeeId(employee.getId(), pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentSubmissionResponse> getAllSubmissions(Pageable pageable) {
        return paymentSubmissionRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentSubmissionResponse> getSubmissionsByStatus(String status, Pageable pageable) {
        return paymentSubmissionRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentSubmissionResponse getSubmission(Long id, Authentication auth) {
        PaymentSubmission submission = paymentSubmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment submission not found with id: " + id));

        boolean isStaff = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN") || a.getAuthority().contains("MANAGER"));
        if (!isStaff) {
            Employee employee = resolveEmployeeFromAuth(auth);
            if (!submission.getEmployee().getId().equals(employee.getId())) {
                throw new AccessDeniedException("Access denied.");
            }
        }
        return toResponse(submission);
    }

    @Override
    @Transactional
    public PaymentSubmissionResponse reviewSubmission(Long id, PaymentSubmissionReviewRequest request, Authentication auth) {
        PaymentSubmission submission = paymentSubmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment submission not found with id: " + id));

        if (!"APPROVED".equals(request.getStatus()) && !"REJECTED".equals(request.getStatus())) {
            throw new IllegalArgumentException("Status must be APPROVED or REJECTED");
        }

        submission.setStatus(request.getStatus());
        submission.setReviewedBy(auth != null ? auth.getName() : "System");
        submission.setReviewNotes(request.getReviewNotes());
        submission.setReviewedAt(LocalDateTime.now());
        submission.setUpdatedAt(LocalDateTime.now());

        if ("APPROVED".equals(request.getStatus())) {
            Customer customer = submission.getCustomer() != null ? submission.getCustomer()
                    : (submission.getProject() != null ? submission.getProject().getCustomer() : null);

            Invoice linkedInvoice = submission.getLinkedInvoice();
            if (linkedInvoice != null) {
                BigDecimal currentPaid = linkedInvoice.getPaidAmount() != null ? linkedInvoice.getPaidAmount() : BigDecimal.ZERO;
                BigDecimal updatedPaid = currentPaid.add(submission.getAmount());
                linkedInvoice.setPaidAmount(updatedPaid);
                BigDecimal updatedPending = linkedInvoice.getTotalAmount() != null
                        ? linkedInvoice.getTotalAmount().subtract(updatedPaid).max(BigDecimal.ZERO)
                        : BigDecimal.ZERO;
                linkedInvoice.setPendingAmount(updatedPending);
                if (updatedPending.compareTo(BigDecimal.ZERO) <= 0) {
                    linkedInvoice.setStatus(com.webliix.finance.enums.InvoiceStatus.PAID);
                } else {
                    linkedInvoice.setStatus(com.webliix.finance.enums.InvoiceStatus.PARTIALLY_PAID);
                }
                linkedInvoice.setUpdatedAt(LocalDateTime.now());
                invoiceRepository.save(linkedInvoice);
            } else {
                String prefix = com.webliix.finance.util.InvoiceNumberGenerator.currentPrefix();
                String invoiceNumber = invoiceRepository.findTopByInvoiceNumberStartingWithOrderByIdDesc(prefix)
                        .map(i -> com.webliix.finance.util.InvoiceNumberGenerator.next(i.getInvoiceNumber()))
                        .orElse(prefix + "000001");

                Invoice newInvoice = Invoice.builder()
                        .invoiceNumber(invoiceNumber)
                        .customer(customer)
                        .project(submission.getProject())
                        .issueDate(submission.getPaymentDate() != null ? submission.getPaymentDate() : LocalDate.now())
                        .dueDate(submission.getPaymentDate() != null ? submission.getPaymentDate() : LocalDate.now())
                        .subtotal(submission.getAmount())
                        .taxAmount(BigDecimal.ZERO)
                        .discountAmount(BigDecimal.ZERO)
                        .totalAmount(submission.getAmount())
                        .paidAmount(submission.getAmount())
                        .pendingAmount(BigDecimal.ZERO)
                        .status(com.webliix.finance.enums.InvoiceStatus.PAID)
                        .notes(submission.getNotes() != null && !submission.getNotes().isBlank()
                                ? submission.getNotes()
                                : "Generated automatically from approved employee billing submission.")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                com.webliix.finance.entity.InvoiceItem item = com.webliix.finance.entity.InvoiceItem.builder()
                        .itemName(submission.getNotes() != null && !submission.getNotes().isBlank() ? submission.getNotes() : "Project Deliverable Work Billing")
                        .description("Payment verified and settled by finance administration. Ref: " + (submission.getReferenceNumber() != null ? submission.getReferenceNumber() : "SUB-" + submission.getId()))
                        .quantity(1)
                        .unitPrice(submission.getAmount())
                        .totalPrice(submission.getAmount())
                        .invoice(newInvoice)
                        .build();

                newInvoice.setItems(new java.util.ArrayList<>(java.util.List.of(item)));
                Invoice savedInvoice = invoiceRepository.save(newInvoice);
                submission.setLinkedInvoice(savedInvoice);

                // Record in ledger payments table
                try {
                    String payPrefix = com.webliix.finance.util.PaymentNumberGenerator.currentPrefix();
                    com.webliix.finance.payment.entity.Payment lastPayment = paymentRepository.findTopByPaymentNumberStartingWithOrderByIdDesc(payPrefix);
                    String payNum = com.webliix.finance.util.PaymentNumberGenerator.next(lastPayment != null ? lastPayment.getPaymentNumber() : null);

                    com.webliix.finance.enums.PaymentMethod pm = com.webliix.finance.enums.PaymentMethod.BANK_TRANSFER;
                    if (submission.getPaymentMethod() != null) {
                        try {
                            pm = com.webliix.finance.enums.PaymentMethod.valueOf(submission.getPaymentMethod().toUpperCase());
                        } catch (Exception ignored) {}
                    }

                    com.webliix.finance.payment.entity.Payment payRecord = com.webliix.finance.payment.entity.Payment.builder()
                            .paymentNumber(payNum)
                            .invoice(savedInvoice)
                            .customer(customer)
                            .amount(submission.getAmount())
                            .paymentDate(submission.getPaymentDate() != null ? submission.getPaymentDate() : LocalDate.now())
                            .paymentMethod(pm)
                            .status(com.webliix.finance.enums.PaymentStatus.SUCCESS)
                            .transactionReference(submission.getReferenceNumber())
                            .remarks(submission.getNotes())
                            .createdAt(LocalDateTime.now())
                            .build();
                    paymentRepository.save(payRecord);
                } catch (Exception payEx) {
                    log.warn("Could not record payment ledger entity: {}", payEx.getMessage());
                }

                // Email customer receipt with Webliix Team
                if (customer != null && customer.getEmail() != null && !customer.getEmail().isBlank()) {
                    try {
                        String custName = customer.getContactPerson() != null && !customer.getContactPerson().isBlank()
                                ? customer.getContactPerson()
                                : (customer.getCompanyName() != null ? customer.getCompanyName() : "Valued Customer");
                        String emailSubject = "Payment Receipt & Invoice Paid – " + savedInvoice.getInvoiceNumber();
                        String htmlReceipt = null;
                        try (java.io.InputStream is = getClass().getResourceAsStream("/templates/emails/invoice-paid.html")) {
                            if (is != null) {
                                htmlReceipt = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)
                                        .replace("[CUSTOMER_NAME]", custName)
                                        .replace("[INVOICE_ID]", savedInvoice.getInvoiceNumber())
                                        .replace("[AMOUNT]", submission.getCurrency() + " " + submission.getAmount());
                            }
                        }
                        if (htmlReceipt != null) {
                            emailService.sendAutomatedHtmlEmail(customer.getEmail(), emailSubject, htmlReceipt);
                        }
                    } catch (Exception emailEx) {
                        log.warn("Could not dispatch customer invoice receipt email: {}", emailEx.getMessage());
                    }
                }
            }

            // Update project budget tracking
            if (submission.getProject() != null) {
                Project proj = submission.getProject();
                BigDecimal currentBudget = proj.getBudget() != null ? proj.getBudget() : BigDecimal.ZERO;
                proj.setBudget(currentBudget.add(submission.getAmount()));
                projectRepository.save(proj);
            }
        }

        PaymentSubmission saved = paymentSubmissionRepository.save(submission);

        auditService.record(AuditAction.APPROVE, AuditModule.FINANCE, "PaymentSubmission",
                saved.getId().toString(), null, saved, request.getStatus(), null);

        // Notify the employee about approval/rejection
        try {
            if (submission.getEmployee() != null && submission.getEmployee().getEmail() != null) {
                CreateNotificationRequest notif = new CreateNotificationRequest();
                notif.setTitle("Payment Submission " + request.getStatus());
                notif.setMessage("Your payment submission of " + submission.getCurrency() + " " +
                        submission.getAmount() + " has been " + request.getStatus().toLowerCase() +
                        (request.getReviewNotes() != null ? ": " + request.getReviewNotes() : "."));
                notif.setRecipient(submission.getEmployee().getEmail());
                notif.setRecipientType("USER");
                notif.setChannel(NotificationChannel.IN_APP);
                notif.setReferenceType("PAYMENT_SUBMISSION");
                notif.setReferenceId(submission.getId());
                notificationService.createNotification(notif);
            }
        } catch (Exception e) {
            log.warn("Failed to send notification for payment submission review: {}", e.getMessage());
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteSubmission(Long id, Authentication auth) {
        PaymentSubmission submission = paymentSubmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment submission not found with id: " + id));

        boolean isStaff = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN") || a.getAuthority().contains("MANAGER"));
        if (!isStaff) {
            Employee employee = resolveEmployeeFromAuth(auth);
            if (!submission.getEmployee().getId().equals(employee.getId())) {
                throw new AccessDeniedException("You can only delete your own submissions.");
            }
            if (!"PENDING_REVIEW".equals(submission.getStatus())) {
                throw new IllegalStateException("Cannot delete a submission that has already been reviewed.");
            }
        }

        auditService.record(AuditAction.DELETE, AuditModule.FINANCE, "PaymentSubmission",
                id.toString(), submission, null, "SUCCESS", null);

        paymentSubmissionRepository.deleteById(id);
    }

    private PaymentSubmissionResponse toResponse(PaymentSubmission s) {
        PaymentSubmissionResponse res = new PaymentSubmissionResponse();
        res.setId(s.getId());
        if (s.getEmployee() != null) {
            res.setEmployeeId(s.getEmployee().getId());
            res.setEmployeeName(s.getEmployee().getFirstName() + " " + s.getEmployee().getLastName());
        }
        if (s.getProject() != null) {
            res.setProjectId(s.getProject().getId());
            res.setProjectName(s.getProject().getProjectName());
        }
        if (s.getCustomer() != null) {
            res.setCustomerId(s.getCustomer().getId());
            res.setCustomerName(s.getCustomer().getCompanyName());
        }
        res.setAmount(s.getAmount());
        res.setCurrency(s.getCurrency());
        res.setPaymentDate(s.getPaymentDate());
        res.setPaymentMethod(s.getPaymentMethod());
        res.setReferenceNumber(s.getReferenceNumber());
        res.setPayerName(s.getPayerName());
        res.setReceiverDetails(s.getReceiverDetails());
        res.setNotes(s.getNotes());
        res.setStatus(s.getStatus());
        res.setReviewedBy(s.getReviewedBy());
        res.setReviewNotes(s.getReviewNotes());
        res.setReviewedAt(s.getReviewedAt());
        if (s.getLinkedInvoice() != null) {
            res.setLinkedInvoiceId(s.getLinkedInvoice().getId());
            res.setLinkedInvoiceNumber(s.getLinkedInvoice().getInvoiceNumber());
        }
        res.setCreatedAt(s.getCreatedAt());
        res.setUpdatedAt(s.getUpdatedAt());
        return res;
    }
}
