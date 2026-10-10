package com.webliix.crm.lead.service;

import com.webliix.crm.customer.entity.Customer;

public interface LeadConversionService {

    Customer convertLead(Long leadId, String customPassword);

    default Customer convertLead(Long leadId) {
        return convertLead(leadId, null);
    }
}
