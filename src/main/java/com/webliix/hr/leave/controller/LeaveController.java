package com.webliix.hr.leave.controller;

import com.webliix.hr.leave.dto.LeaveBalanceRequest;
import com.webliix.hr.leave.dto.LeaveBalanceResponse;
import com.webliix.hr.leave.dto.LeaveRequestDto;
import com.webliix.hr.leave.dto.LeaveResponse;
import com.webliix.hr.leave.service.LeaveBalanceService;
import com.webliix.hr.leave.service.LeaveService;
import com.webliix.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final LeaveBalanceService leaveBalanceService;

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveResponse>> applyLeave(@RequestBody LeaveRequestDto request) {
        LeaveResponse response = leaveService.applyLeave(request);
        return ResponseEntity.ok(ApiResponse.<LeaveResponse>builder().success(true).message("Leave request created").data(response).build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<LeaveResponse>>> getLeaves(@RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveResponse> response = leaveService.getAllLeaves(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<LeaveResponse>>builder().success(true).message("Leave requests fetched").data(response).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveResponse>> getLeave(@PathVariable Long id) {
        LeaveResponse response = leaveService.getLeave(id);
        return ResponseEntity.ok(ApiResponse.<LeaveResponse>builder().success(true).message("Leave request fetched").data(response).build());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<LeaveResponse>> approveLeave(@PathVariable Long id) {
        LeaveResponse response = leaveService.approveLeave(id);
        return ResponseEntity.ok(ApiResponse.<LeaveResponse>builder().success(true).message("Leave request approved").data(response).build());
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<LeaveResponse>> rejectLeave(@PathVariable Long id) {
        LeaveResponse response = leaveService.rejectLeave(id);
        return ResponseEntity.ok(ApiResponse.<LeaveResponse>builder().success(true).message("Leave request rejected").data(response).build());
    }

    @PostMapping("/balances")
    public ResponseEntity<ApiResponse<LeaveBalanceResponse>> createLeaveBalance(@RequestBody LeaveBalanceRequest request) {
        LeaveBalanceResponse response = leaveBalanceService.createLeaveBalance(request);
        return ResponseEntity.ok(ApiResponse.<LeaveBalanceResponse>builder().success(true).message("Leave balance created").data(response).build());
    }

    @GetMapping("/balances")
    public ResponseEntity<ApiResponse<Page<LeaveBalanceResponse>>> getLeaveBalances(@RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveBalanceResponse> response = leaveBalanceService.getAllLeaveBalances(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<LeaveBalanceResponse>>builder().success(true).message("Leave balances fetched").data(response).build());
    }

    @GetMapping("/balances/{id}")
    public ResponseEntity<ApiResponse<LeaveBalanceResponse>> getLeaveBalance(@PathVariable Long id) {
        LeaveBalanceResponse response = leaveBalanceService.getLeaveBalance(id);
        return ResponseEntity.ok(ApiResponse.<LeaveBalanceResponse>builder().success(true).message("Leave balance fetched").data(response).build());
    }
}
