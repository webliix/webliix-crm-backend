package com.webliix.crm.customer.service;

import com.webliix.crm.customer.dto.CustomerStatisticsResponse;
import com.webliix.crm.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class CustomerStatisticsService {

    private final CustomerRepository customerRepository;

    public CustomerStatisticsResponse getStatistics() {
        long totalCustomers = customerRepository.count();
        long activeCustomers = customerRepository.countByActiveTrue();
        long inactiveCustomers = customerRepository.countByActiveFalse();
        LocalDate startOfMonth = YearMonth.now().atDay(1);
        long newThisMonth = customerRepository.countByCustomerSinceAfter(startOfMonth.minusDays(1));

        return CustomerStatisticsResponse.builder()
                .totalCustomers(totalCustomers)
                .activeCustomers(activeCustomers)
                .inactiveCustomers(inactiveCustomers)
                .newThisMonth(newThisMonth)
                .build();
    }
}
