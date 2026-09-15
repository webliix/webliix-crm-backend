package com.webliix.crm.lead.repository;

import com.webliix.crm.lead.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    Page<Lead> findByCompanyNameContainingIgnoreCase(String companyName, Pageable pageable);
}
