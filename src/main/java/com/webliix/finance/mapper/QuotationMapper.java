package com.webliix.finance.mapper;

import com.webliix.finance.dto.*;
import com.webliix.finance.entity.Quotation;
import com.webliix.finance.entity.QuotationItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class QuotationMapper {

    public static Quotation toEntity(CreateQuotationRequest req) {
        Quotation q = Quotation.builder()
                .issueDate(req.getIssueDate())
                .validTill(req.getValidTill())
                .taxAmount(req.getTaxAmount())
                .discount(req.getDiscount())
                .notes(req.getNotes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (req.getItems() != null) {
            List<QuotationItem> items = req.getItems().stream().map(i -> {
                QuotationItem it = QuotationItem.builder()
                        .itemName(i.getItemName())
                        .description(i.getDescription())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .totalPrice(i.getUnitPrice() == null ? null : i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()==null?0:i.getQuantity())))
                        .quotation(q)
                        .build();
                return it;
            }).collect(Collectors.toList());
            q.setItems(items);
            BigDecimal subtotal = items.stream().map(QuotationItem::getTotalPrice).filter(p -> p!=null).reduce(BigDecimal.ZERO, BigDecimal::add);
            q.setSubtotal(subtotal);
            BigDecimal tax = req.getTaxAmount()==null?BigDecimal.ZERO:req.getTaxAmount();
            BigDecimal discount = req.getDiscount()==null?BigDecimal.ZERO:req.getDiscount();
            q.setTaxAmount(tax);
            q.setDiscount(discount);
            q.setTotalAmount(subtotal.add(tax).subtract(discount));
        }
        q.setStatus(req.getStatus() == null ? com.webliix.finance.enums.QuotationStatus.DRAFT : req.getStatus());
        q.setConverted(false);

        return q;
    }

    public static QuotationResponse toResponse(Quotation q) {
        QuotationResponse res = new QuotationResponse();
        res.setId(q.getId());
        res.setQuotationNumber(q.getQuotationNumber());
        res.setCustomerId(q.getCustomer()==null?null:q.getCustomer().getId());
        res.setProjectId(q.getProject()==null?null:q.getProject().getId());
        res.setIssueDate(q.getIssueDate());
        res.setValidTill(q.getValidTill());
        res.setSubtotal(q.getSubtotal());
        res.setTaxAmount(q.getTaxAmount());
        res.setDiscount(q.getDiscount());
        res.setTotalAmount(q.getTotalAmount());
        res.setStatus(q.getStatus());
        res.setNotes(q.getNotes());
        if (q.getItems() != null) {
            List<QuotationItemResponse> items = q.getItems().stream().map(it -> {
                QuotationItemResponse r = new QuotationItemResponse();
                r.setId(it.getId());
                r.setItemName(it.getItemName());
                r.setDescription(it.getDescription());
                r.setQuantity(it.getQuantity());
                r.setUnitPrice(it.getUnitPrice());
                r.setTotalPrice(it.getTotalPrice());
                return r;
            }).collect(Collectors.toList());
            res.setItems(items);
        }
        res.setConverted(q.getConverted());
        res.setConvertedAt(q.getConvertedAt());
        res.setCreatedAt(q.getCreatedAt());
        res.setUpdatedAt(q.getUpdatedAt());
        return res;
    }
}
