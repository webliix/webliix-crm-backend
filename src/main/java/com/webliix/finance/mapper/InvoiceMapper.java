package com.webliix.finance.mapper;

import com.webliix.finance.dto.CreateInvoiceRequest;
import com.webliix.finance.dto.InvoiceItemResponse;
import com.webliix.finance.dto.InvoiceResponse;
import com.webliix.finance.entity.Invoice;
import com.webliix.finance.entity.InvoiceItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceMapper {

    public static Invoice toEntity(CreateInvoiceRequest req) {
        Invoice invoice = Invoice.builder()
                .issueDate(req.getIssueDate())
                .dueDate(req.getDueDate())
                .taxAmount(req.getTaxAmount())
                .discountAmount(req.getDiscountAmount())
                .notes(req.getNotes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        if (req.getItems() != null) {
            List<InvoiceItem> items = req.getItems().stream().map(i -> {
                InvoiceItem item = InvoiceItem.builder()
                        .itemName(i.getItemName())
                        .description(i.getDescription())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .totalPrice(i.getUnitPrice() == null ? BigDecimal.ZERO : i.getUnitPrice().multiply(i.getQuantity() == null ? BigDecimal.ZERO : BigDecimal.valueOf(i.getQuantity())))
                        .invoice(invoice)
                        .build();
                return item;
            }).collect(Collectors.toList());
            invoice.setItems(items);
            subtotal = items.stream().map(InvoiceItem::getTotalPrice).filter(p -> p != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal tax = req.getTaxAmount() == null ? BigDecimal.ZERO : req.getTaxAmount();
        BigDecimal discount = req.getDiscountAmount() == null ? BigDecimal.ZERO : req.getDiscountAmount();
        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(tax);
        invoice.setDiscountAmount(discount);
        invoice.setTotalAmount(subtotal.add(tax).subtract(discount));
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setPendingAmount(invoice.getTotalAmount());
        invoice.setStatus(com.webliix.finance.enums.InvoiceStatus.DRAFT);

        return invoice;
    }

    public static InvoiceResponse toResponse(Invoice invoice) {
        InvoiceResponse res = new InvoiceResponse();
        res.setId(invoice.getId());
        res.setInvoiceNumber(invoice.getInvoiceNumber());
        res.setCustomerId(invoice.getCustomer() == null ? null : invoice.getCustomer().getId());
        res.setProjectId(invoice.getProject() == null ? null : invoice.getProject().getId());
        res.setIssueDate(invoice.getIssueDate());
        res.setDueDate(invoice.getDueDate());
        res.setSubtotal(invoice.getSubtotal());
        res.setTaxAmount(invoice.getTaxAmount());
        res.setDiscountAmount(invoice.getDiscountAmount());
        res.setTotalAmount(invoice.getTotalAmount());
        res.setPaidAmount(invoice.getPaidAmount());
        res.setPendingAmount(invoice.getPendingAmount());
        res.setStatus(invoice.getStatus());
        res.setNotes(invoice.getNotes());
        if (invoice.getItems() != null) {
            res.setItems(invoice.getItems().stream().map(item -> {
                InvoiceItemResponse ir = new InvoiceItemResponse();
                ir.setId(item.getId());
                ir.setItemName(item.getItemName());
                ir.setDescription(item.getDescription());
                ir.setQuantity(item.getQuantity());
                ir.setUnitPrice(item.getUnitPrice());
                ir.setTotalPrice(item.getTotalPrice());
                return ir;
            }).collect(Collectors.toList()));
        }
        res.setCreatedAt(invoice.getCreatedAt());
        res.setUpdatedAt(invoice.getUpdatedAt());
        return res;
    }
}
