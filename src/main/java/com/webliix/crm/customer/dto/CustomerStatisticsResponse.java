package com.webliix.crm.customer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerStatisticsResponse {

    private long totalCustomers;
    private long activeCustomers;
    private long inactiveCustomers;
    private long newThisMonth;
}
