package com.webliix.hr.designation.repository;

import com.webliix.hr.designation.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {
}
