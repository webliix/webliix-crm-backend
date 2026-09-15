package com.webliix.crm.customer.repository;

import com.webliix.crm.customer.entity.CustomerContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerContactRepository extends JpaRepository<CustomerContact, Long> {

    List<CustomerContact> findByCustomerId(Long customerId);
}
