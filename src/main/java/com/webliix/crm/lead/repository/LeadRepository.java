package com.webliix.crm.lead.repository;

import com.webliix.crm.lead.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    Page<Lead> findByCompanyNameContainingIgnoreCase(String companyName, Pageable pageable);

    @Query("SELECT l FROM Lead l WHERE LOWER(l.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(l.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(l.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(l.requirements) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Lead> searchLeads(@Param("keyword") String keyword, Pageable pageable);
}
