package com.webliix.finance.payment.service.impl;

import com.webliix.finance.enums.InvoiceStatus;
import com.webliix.finance.enums.PaymentStatus;
import com.webliix.finance.entity.Invoice;
import com.webliix.finance.payment.dto.PaymentRequest;
import com.webliix.finance.payment.dto.PaymentResponse;
import com.webliix.finance.payment.entity.Payment;
import com.webliix.finance.payment.mapper.PaymentMapper;
import com.webliix.finance.payment.repository.PaymentRepository;
import com.webliix.finance.payment.service.PaymentService;
import com.webliix.finance.repository.InvoiceRepository;
import com.webliix.finance.util.PaymentNumberGenerator;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public PaymentResponse recordPayment(Long invoiceId, PaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        if (invoice.getTotalAmount() == null) {
            throw new IllegalArgumentException("Invoice total amount is not available");
        }

        if (invoice.getPaidAmount() == null) {
            invoice.setPaidAmount(BigDecimal.ZERO);
        }

        if (invoice.getPendingAmount() == null) {
            invoice.setPendingAmount(invoice.getTotalAmount().subtract(invoice.getPaidAmount()));
        }

        if (request.getAmount().compareTo(invoice.getPendingAmount()) > 0) {
            throw new IllegalArgumentException("Overpayment is not allowed");
        }

        BigDecimal paidAmount = invoice.getPaidAmount().add(request.getAmount());
        BigDecimal pendingAmount = invoice.getTotalAmount().subtract(paidAmount);

        if (paidAmount.compareTo(invoice.getTotalAmount()) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoice.setPaidAmount(paidAmount);
        invoice.setPendingAmount(pendingAmount);
        invoice.setUpdatedAt(LocalDateTime.now());
        invoiceRepository.save(invoice);

        String prefix = PaymentNumberGenerator.currentPrefix();
        Payment last = paymentRepository.findTopByPaymentNumberStartingWithOrderByIdDesc(prefix);
        String paymentNumber = last != null
                ? PaymentNumberGenerator.next(last.getPaymentNumber())
                : prefix + "000001";

        Payment payment = Payment.builder()
                .paymentNumber(paymentNumber)
                .invoice(invoice)
                .customer(invoice.getCustomer())
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.SUCCESS)
                .transactionReference(request.getTransactionReference())
                .remarks(request.getRemarks())
                .createdAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);
        return PaymentMapper.toResponse(saved);
    }

    @Override
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(PaymentMapper::toResponse);
    }

    @Override
    public PaymentResponse getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return PaymentMapper.toResponse(payment);
    }

    @Override
    public Page<PaymentResponse> getPaymentsByCustomer(Long customerId, Pageable pageable) {
        return paymentRepository.findByCustomerId(customerId, pageable).map(PaymentMapper::toResponse);
    }

    @Override
    public Page<PaymentResponse> getPaymentsByInvoice(Long invoiceId, Pageable pageable) {
        return paymentRepository.findByInvoiceId(invoiceId, pageable).map(PaymentMapper::toResponse);
    }
}
