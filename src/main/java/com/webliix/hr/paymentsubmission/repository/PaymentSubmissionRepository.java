package com.webliix.hr.paymentsubmission.repository;

import com.webliix.hr.paymentsubmission.entity.PaymentSubmission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentSubmissionRepository extends JpaRepository<PaymentSubmission, Long> {
    Page<PaymentSubmission> findByEmployeeId(Long employeeId, Pageable pageable);
    Page<PaymentSubmission> findByStatus(String status, Pageable pageable);
    Page<PaymentSubmission> findByProjectId(Long projectId, Pageable pageable);
    List<PaymentSubmission> findByProjectId(Long projectId);
    List<PaymentSubmission> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
