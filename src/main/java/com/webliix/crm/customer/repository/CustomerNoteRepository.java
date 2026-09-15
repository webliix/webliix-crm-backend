package com.webliix.crm.customer.repository;

import com.webliix.crm.customer.entity.CustomerNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerNoteRepository extends JpaRepository<CustomerNote, Long> {

    List<CustomerNote> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
