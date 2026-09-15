package com.webliix.finance.repository;

import com.webliix.finance.entity.Quotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {
    Page<Quotation> findAll(Pageable pageable);
    Optional<Quotation> findTopByQuotationNumberStartingWithOrderByIdDesc(String prefix);
}
