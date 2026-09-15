package com.webliix.finance.service.impl;

import com.webliix.finance.entity.Invoice;
import com.webliix.finance.entity.InvoiceItem;
import com.webliix.finance.entity.Quotation;
import com.webliix.finance.enums.InvoiceStatus;
import com.webliix.finance.repository.InvoiceRepository;
import com.webliix.finance.repository.QuotationRepository;
import com.webliix.finance.service.InvoiceConversionService;
import com.webliix.finance.util.InvoiceNumberGenerator;
import com.webliix.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuotationConversionServiceImpl implements InvoiceConversionService {

    private final QuotationRepository quotationRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public Invoice convertToInvoice(Long quotationId) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new ResourceNotFoundException("Quotation not found"));

        if (quotation.getStatus() != com.webliix.finance.enums.QuotationStatus.APPROVED) {
            throw new IllegalStateException("Quotation must be APPROVED before conversion");
        }
        if (Boolean.TRUE.equals(quotation.getConverted())) {
            throw new IllegalStateException("Quotation has already been converted");
        }

        Invoice invoice = Invoice.builder()
                .customer(quotation.getCustomer())
                .project(quotation.getProject())
                .issueDate(quotation.getIssueDate())
                .dueDate(quotation.getValidTill())
                .subtotal(quotation.getSubtotal())
                .taxAmount(quotation.getTaxAmount())
                .discountAmount(quotation.getDiscount())
                .totalAmount(quotation.getTotalAmount())
                .paidAmount(java.math.BigDecimal.ZERO)
                .pendingAmount(quotation.getTotalAmount())
                .status(InvoiceStatus.SENT)
                .notes(quotation.getNotes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<InvoiceItem> items = quotation.getItems() == null ? List.of() : quotation.getItems().stream().map(qItem -> {
            InvoiceItem invoiceItem = InvoiceItem.builder()
                    .itemName(qItem.getItemName())
                    .description(qItem.getDescription())
                    .quantity(qItem.getQuantity())
                    .unitPrice(qItem.getUnitPrice())
                    .totalPrice(qItem.getTotalPrice())
                    .invoice(invoice)
                    .build();
            return invoiceItem;
        }).collect(Collectors.toList());
        invoice.setItems(items);

        String prefix = InvoiceNumberGenerator.currentPrefix();
        String nextInvoiceNumber = invoiceRepository.findTopByInvoiceNumberStartingWithOrderByIdDesc(prefix)
                .map(i -> InvoiceNumberGenerator.next(i.getInvoiceNumber()))
                .orElse(prefix + "000001");
        invoice.setInvoiceNumber(nextInvoiceNumber);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        quotation.setConverted(true);
        quotation.setConvertedAt(LocalDateTime.now());
        quotationRepository.save(quotation);

        return savedInvoice;
    }
}
