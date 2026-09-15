package com.webliix.crm.lead.service;

import com.webliix.crm.customer.entity.Customer;

public interface LeadConversionService {

    Customer convertLead(Long leadId);
}
