package com.webliix.crm.customer.repository;

import com.webliix.crm.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByCompanyNameContainingIgnoreCase(String companyName, Pageable pageable);

    Optional<Customer> findByCustomerCode(String customerCode);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findTopByOrderByIdDesc();

    long countByActiveTrue();

    long countByActiveFalse();

    long countByCustomerSinceAfter(java.time.LocalDate date);
}
