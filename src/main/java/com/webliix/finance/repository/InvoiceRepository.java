package com.webliix.finance.repository;

import com.webliix.finance.entity.Invoice;
import com.webliix.finance.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findTopByInvoiceNumberStartingWithOrderByIdDesc(String prefix);

    List<Invoice> findByCustomerId(Long customerId);
    Page<Invoice> findByCustomerId(Long customerId, Pageable pageable);

    List<Invoice> findByProjectId(Long projectId);
    Page<Invoice> findByProjectId(Long projectId, Pageable pageable);
    List<Invoice> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    Page<Invoice> findByCustomerEmail(String email, Pageable pageable);

    Page<Invoice> findByCustomerEmailAndInvoiceNumberContainingIgnoreCaseOrCustomerEmailAndProjectProjectNameContainingIgnoreCase(
            String email1, String invoiceNumber, String email2, String projectName, Pageable pageable);

    Page<Invoice> findByInvoiceNumberContainingIgnoreCaseOrCustomerCompanyNameContainingIgnoreCaseOrProjectProjectNameContainingIgnoreCase(
            String invoiceNumber,
            String customerName,
            String projectName,
            Pageable pageable);

    long countByStatus(InvoiceStatus status);

    long countByStatusNot(InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i")
    BigDecimal sumTotalAmount();

    @Query("SELECT COALESCE(SUM(i.paidAmount), 0) FROM Invoice i")
    BigDecimal sumPaidAmount();

    @Query("SELECT COALESCE(SUM(i.pendingAmount), 0) FROM Invoice i")
    BigDecimal sumPendingAmount();

    @Query("SELECT EXTRACT(MONTH FROM i.issueDate), COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.issueDate IS NOT NULL GROUP BY EXTRACT(MONTH FROM i.issueDate)")
    List<Object[]> sumTotalAmountGroupByMonth();
}
