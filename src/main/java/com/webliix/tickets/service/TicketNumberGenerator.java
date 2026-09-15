package com.webliix.tickets.service;

import com.webliix.tickets.entity.Ticket;
import com.webliix.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TicketNumberGenerator {

    private final TicketRepository ticketRepository;

    public String generateNextTicketNumber() {
        int currentYear = java.time.Year.now().getValue();
        String prefix = "TKT-" + currentYear + "-";
        Optional<Ticket> latest = ticketRepository.findTopByOrderByIdDesc();
        if (latest.isEmpty() || latest.get().getTicketNumber() == null) {
            return prefix + "000001";
        }
        String lastNumber = latest.get().getTicketNumber();
        try {
            if (lastNumber.startsWith("TKT-" + currentYear + "-")) {
                String numericPart = lastNumber.substring(lastNumber.lastIndexOf('-') + 1);
                int next = Integer.parseInt(numericPart) + 1;
                return prefix + String.format("%06d", next);
            }
        } catch (Exception ignored) {
            // fallback below
        }
        return prefix + "000001";
    }
}
