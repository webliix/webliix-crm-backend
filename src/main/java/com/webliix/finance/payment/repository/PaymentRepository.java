package com.webliix.finance.payment.repository;

import com.webliix.finance.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByInvoiceId(Long invoiceId, Pageable pageable);
    Page<Payment> findByCustomerId(Long customerId, Pageable pageable);
    Page<Payment> findByPaymentNumberContainingIgnoreCase(String paymentNumber, Pageable pageable);
    Payment findTopByPaymentNumberStartingWithOrderByIdDesc(String prefix);
}
