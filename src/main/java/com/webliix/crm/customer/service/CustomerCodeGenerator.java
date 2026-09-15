package com.webliix.crm.customer.service;

import com.webliix.crm.customer.entity.Customer;
import com.webliix.crm.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerCodeGenerator {

    private final CustomerRepository customerRepository;

    public String generateNextCustomerCode() {
        Optional<Customer> latestCustomer = customerRepository.findTopByOrderByIdDesc();
        long nextNumber = latestCustomer.map(Customer::getId).orElse(0L) + 1;
        return String.format("CUS-%06d", nextNumber);
    }
}
