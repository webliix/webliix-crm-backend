package com.webliix.hr.designation.controller;

import com.webliix.hr.designation.dto.DesignationRequest;
import com.webliix.hr.designation.dto.DesignationResponse;
import com.webliix.hr.designation.service.DesignationService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService designationService;

    @PostMapping
    public ResponseEntity<ApiResponse<DesignationResponse>> createDesignation(@RequestBody DesignationRequest request) {
        DesignationResponse response = designationService.createDesignation(request);
        return ResponseEntity.ok(ApiResponse.<DesignationResponse>builder().success(true).message("Designation created successfully").data(response).build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DesignationResponse>>> getDesignations(@RequestParam(defaultValue = "0") int page,
                                                                                  @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DesignationResponse> response = designationService.getAllDesignations(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<DesignationResponse>>builder().success(true).message("Designations fetched").data(response).build());
    }
}
