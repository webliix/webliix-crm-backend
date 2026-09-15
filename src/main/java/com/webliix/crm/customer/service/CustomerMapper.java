package com.webliix.crm.customer.service;

import com.webliix.crm.customer.dto.CreateCustomerRequest;
import com.webliix.crm.customer.dto.CustomerResponse;
import com.webliix.crm.customer.entity.Customer;

import java.time.LocalDateTime;

public class CustomerMapper {

    public static Customer toEntity(CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setCompanyName(request.getCompanyName());
        customer.setContactPerson(request.getContactPerson());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setWebsite(request.getWebsite());
        customer.setGstNumber(request.getGstNumber());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setCountry(request.getCountry());
        customer.setLifetimeValue(request.getLifetimeValue());
        customer.setCustomerSince(request.getCustomerSince());
        customer.setActive(request.getActive() != null ? request.getActive() : true);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        return customer;
    }

    public static CustomerResponse toResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setCompanyName(customer.getCompanyName());
        response.setCustomerCode(customer.getCustomerCode());
        response.setContactPerson(customer.getContactPerson());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setWebsite(customer.getWebsite());
        response.setGstNumber(customer.getGstNumber());
        response.setAddress(customer.getAddress());
        response.setCity(customer.getCity());
        response.setState(customer.getState());
        response.setCountry(customer.getCountry());
        response.setLifetimeValue(customer.getLifetimeValue());
        response.setCustomerSince(customer.getCustomerSince());
        response.setActive(customer.getActive());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        return response;
    }

    public static void updateEntity(Customer customer, CreateCustomerRequest request) {
        if (request.getCompanyName() != null) {
            customer.setCompanyName(request.getCompanyName());
        }
        if (request.getContactPerson() != null) {
            customer.setContactPerson(request.getContactPerson());
        }
        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getWebsite() != null) {
            customer.setWebsite(request.getWebsite());
        }
        if (request.getGstNumber() != null) {
            customer.setGstNumber(request.getGstNumber());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            customer.setCity(request.getCity());
        }
        if (request.getState() != null) {
            customer.setState(request.getState());
        }
        if (request.getCountry() != null) {
            customer.setCountry(request.getCountry());
        }
        if (request.getLifetimeValue() != null) {
            customer.setLifetimeValue(request.getLifetimeValue());
        }
        if (request.getCustomerSince() != null) {
            customer.setCustomerSince(request.getCustomerSince());
        }
        if (request.getActive() != null) {
            customer.setActive(request.getActive());
        }
        customer.setUpdatedAt(LocalDateTime.now());
    }
}
