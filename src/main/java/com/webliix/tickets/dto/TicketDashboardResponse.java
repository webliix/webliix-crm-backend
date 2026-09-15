package com.webliix.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDashboardResponse {

    private long openTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long criticalTickets;
    private long overdueTickets;
}
