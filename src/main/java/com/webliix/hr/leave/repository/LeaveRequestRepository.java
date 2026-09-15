package com.webliix.hr.leave.repository;

import com.webliix.hr.leave.entity.LeaveRequest;
import com.webliix.hr.leave.enums.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Page<LeaveRequest> findAllByStatus(LeaveStatus status, Pageable pageable);
}
