package com.webliix.finance.repository;

import com.webliix.finance.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findTopByInvoiceNumberStartingWithOrderByIdDesc(String prefix);

    List<Invoice> findByCustomerId(Long customerId);

    Page<Invoice> findByInvoiceNumberContainingIgnoreCaseOrCustomerCompanyNameContainingIgnoreCaseOrProjectProjectNameContainingIgnoreCase(
            String invoiceNumber,
            String customerName,
            String projectName,
            Pageable pageable);
}
