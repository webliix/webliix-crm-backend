package com.webliix.hr.paymentsubmission.service;

import com.webliix.hr.paymentsubmission.dto.PaymentSubmissionRequest;
import com.webliix.hr.paymentsubmission.dto.PaymentSubmissionResponse;
import com.webliix.hr.paymentsubmission.dto.PaymentSubmissionReviewRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface PaymentSubmissionService {
    PaymentSubmissionResponse submitPayment(PaymentSubmissionRequest request, Authentication auth);
    Page<PaymentSubmissionResponse> getMySubmissions(Pageable pageable, Authentication auth);
    Page<PaymentSubmissionResponse> getAllSubmissions(Pageable pageable);
    Page<PaymentSubmissionResponse> getSubmissionsByStatus(String status, Pageable pageable);
    PaymentSubmissionResponse getSubmission(Long id, Authentication auth);
    PaymentSubmissionResponse reviewSubmission(Long id, PaymentSubmissionReviewRequest request, Authentication auth);
    void deleteSubmission(Long id, Authentication auth);
}
