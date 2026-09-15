package com.webliix.hr.designation.service;

import com.webliix.hr.designation.dto.DesignationRequest;
import com.webliix.hr.designation.dto.DesignationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DesignationService {
    DesignationResponse createDesignation(DesignationRequest request);
    Page<DesignationResponse> getAllDesignations(Pageable pageable);
}
